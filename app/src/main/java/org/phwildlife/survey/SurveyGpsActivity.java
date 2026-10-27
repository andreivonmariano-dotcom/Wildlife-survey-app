package org.phwildlife.survey;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Locale;

/**
 * Survey-start GNSS gate. No survey row or survey effort begins here.
 * An accurate fix is a device-reported estimate, not a measured positional guarantee.
 */
public final class SurveyGpsActivity extends Activity implements LocationListener {
    private static final int REQUEST_FINE_LOCATION = 413;
    private LocationManager manager;
    private Location qualifiedFix;
    private TextView status;
    private Button startButton;
    private boolean listening = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int gap = Math.round(18f * getResources().getDisplayMetrics().density);
        root.setPadding(gap, gap, gap, gap);
        root.setBackgroundColor(UiKit.BG);
        root.addView(UiKit.title(this, "Acquire survey-start GPS"));

        TextView details = new TextView(this);
        details.setText("Wait for a fresh, precise GPS fix before the survey clock starts. "
            + "Target: reported horizontal accuracy better than 10 m; fix age at most 10 seconds. "
            + "In forest canopy, this standard may not be reachable. "
            + "Move to clearer sky or retry. The phone's accuracy estimate is not a guarantee of true error.");
        details.setTextSize(15);
        details.setTextColor(UiKit.INK);
        details.setPadding(0, gap, 0, gap);
        root.addView(details);

        status = new TextView(this);
        status.setTextSize(17);
        status.setTextColor(UiKit.INK);
        status.setText("Waiting for a new GPS position...");
        status.setPadding(0, gap, 0, gap);
        root.addView(status);

        startButton = UiKit.button(this, "Start survey with verified GPS", "primary");
        startButton.setEnabled(false);
        startButton.setOnClickListener(v -> launchSurvey());
        root.addView(startButton, new LinearLayout.LayoutParams(-1, -2));

        Button retry = UiKit.button(this, "Retry GPS acquisition", "secondary");
        retry.setOnClickListener(v -> beginAcquisition());
        root.addView(retry);

        Button settings = UiKit.button(this, "Location settings", "secondary");
        settings.setOnClickListener(v -> startActivity(
            new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
        root.addView(settings);

        Button cancel = UiKit.button(this, "Cancel and return home", "secondary");
        cancel.setOnClickListener(v -> finish());
        root.addView(cancel);

        setContentView(root);
    }

    @Override protected void onResume() {
        super.onResume();
        beginAcquisition();
    }

    private void beginAcquisition() {
        stopListening();
        qualifiedFix = null;
        startButton.setEnabled(false);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            status.setText("Allow PRECISE location, not approximate-only location, to start a survey.");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_FINE_LOCATION);
            return;
        }
        if (manager == null || !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            status.setText("GPS is disabled. Turn on phone location services and retry.");
            return;
        }
        status.setText("Searching for satellites. Waiting for a live fix better than 10 m...");
        try {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, this);
            listening = true;
        } catch (SecurityException ex) {
            status.setText("Precise location permission is required.");
        } catch (RuntimeException ex) {
            status.setText("GPS could not start: " + ex.getMessage());
        }
    }

    @Override public void onLocationChanged(Location fix) {
        long now = System.currentTimeMillis();
        boolean valid = LocationManager.GPS_PROVIDER.equals(fix.getProvider())
            && fix.hasAccuracy()
            && GpsFixPolicy.acceptable(fix.getLatitude(), fix.getLongitude(),
                fix.getAccuracy(), fix.getTime(), now, fix.isFromMockProvider());
        qualifiedFix = valid ? new Location(fix) : null;
        startButton.setEnabled(valid);
        String quality = fix.hasAccuracy()
            ? String.format(Locale.US, "±%.1f m", fix.getAccuracy())
            : "unknown accuracy";
        if (fix.isFromMockProvider()) {
            status.setText("Mock location rejected. Acquire a real GPS fix.");
        } else if (valid) {
            status.setText(String.format(Locale.US,
                "GPS ready: %.6f, %.6f\nReported accuracy: %s\nFix age: %.1f s",
                fix.getLatitude(), fix.getLongitude(), quality,
                (now - fix.getTime()) / 1000f));
        } else {
            status.setText("Acquiring better GPS... Current: " + quality
                + " (required: <10 m), fix age "
                + String.format(Locale.US, "%.1f s", (now - fix.getTime()) / 1000f)
                + ". Keep the phone stationary under clear sky.");
        }
    }

    private void launchSurvey() {
        Location fix = qualifiedFix;
        long now = System.currentTimeMillis();
        if (fix == null || !LocationManager.GPS_PROVIDER.equals(fix.getProvider())
            || !fix.hasAccuracy()
            || !GpsFixPolicy.acceptable(fix.getLatitude(), fix.getLongitude(),
                fix.getAccuracy(), fix.getTime(), now, fix.isFromMockProvider())) {
            qualifiedFix = null;
            startButton.setEnabled(false);
            status.setText("GPS fix expired or lost accuracy. Waiting for another live fix.");
            return;
        }
        Intent launch = new Intent(this, MainActivity.class);
        if (getIntent().getExtras() != null) launch.putExtras(getIntent().getExtras());
        launch.putExtra("start_gps_latitude", fix.getLatitude());
        launch.putExtra("start_gps_longitude", fix.getLongitude());
        launch.putExtra("start_gps_accuracy_m", fix.getAccuracy());
        launch.putExtra("start_gps_recorded_at_ms", fix.getTime());
        stopListening();
        startActivity(launch);
        finish();
    }

    private void stopListening() {
        if (!listening || manager == null) return;
        try { manager.removeUpdates(this); } catch (RuntimeException ignored) {}
        listening = false;
    }

    @Override protected void onPause() {
        stopListening();
        super.onPause();
    }
    @Override public void onProviderDisabled(String provider) {
        qualifiedFix = null;
        startButton.setEnabled(false);
        status.setText("GPS provider disabled. Turn on location services.");
    }
    @Override public void onProviderEnabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)) beginAcquisition();
    }
    @SuppressWarnings("deprecation")
    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grants) {
        super.onRequestPermissionsResult(requestCode, permissions, grants);
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) beginAcquisition();
            else status.setText("Precise location access is needed. Grant it in app permissions and retry.");
        }
    }
}
