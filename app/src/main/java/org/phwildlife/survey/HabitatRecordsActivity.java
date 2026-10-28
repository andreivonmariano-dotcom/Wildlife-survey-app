    private void render(){
        if(items==null)return;
        items.removeAllViews();
        SQLiteDatabase db=helper.getReadableDatabase();
        guideTab.setText(selectedTab==0?"Habitat guide  ●":"Habitat guide");
        recordsTab.setText(selectedTab==1?"My site records  ●":"My site records");
        if(selectedTab==0){
            Button filter=UiKit.button(this,"Filter: "+habitatGroup+"  ▾","quiet");
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(50));
            lp.topMargin=dp(8);
            items.addView(filter,lp);
            final String[] groups={"All habitats","Terrestrial","Edaphic",
                "Coastal","Aquatic","Human-modified"};
            filter.setOnClickListener(v->new android.app.AlertDialog.Builder(this)
                .setTitle("Choose habitat group")
                .setItems(groups,(dialog,index)->{
                    habitatGroup=groups[index];render();
                }).setNegativeButton("Cancel",null).show());
            if("All habitats".equals(habitatGroup))
                OfflineFieldLibrary.addHabitats(this,items);
            else OfflineFieldLibrary.addHabitats(this,items,habitatGroup);
            items.addView(FieldVisuals.context(this,
                "Use the habitat descriptions as field reference only. "+
                "Your actual sampling-station measurements remain separate."));
            return;
        }
        Button stations=UiKit.button(this,"Manage projects & sampling stations","secondary");
        stations.setOnClickListener(v->startActivity(
            new Intent(this,SitesActivity.class)));
        LinearLayout.LayoutParams stationLp=new LinearLayout.LayoutParams(-1,dp(52));
        stationLp.topMargin=dp(7);items.addView(stations,stationLp);
        items.addView(FieldVisuals.section(this,"Site habitat register",
            "Human-entered descriptions, not inferred vegetation types"));
        try(Cursor c=db.rawQuery("SELECT id,name,COALESCE(habitat,''),"+
                "COALESCE(project_id,'') FROM sites ORDER BY updated_at DESC LIMIT 35",null)){
            int seen=0;
            while(c.moveToNext()){
                String habitat=value(c,2,"Habitat not recorded");
                items.addView(FieldVisuals.context(this,value(c,1,c.getString(0))+
                    " · "+habitat+"\nProject: "+value(c,3,"Unassigned")));
                seen++;
            }
            if(seen==0)items.addView(label("No registered stations yet.",14));
        }catch(Exception err){items.addView(label("Site records unavailable.",14));}
        items.addView(FieldVisuals.section(this,"Recorded environmental visits",
            "Up to 35 recent sampling units; missing is not zero"));
        try(Cursor c=db.rawQuery(
                "SELECT COALESCE(station_id,''),COALESCE(habitat_code,''),"+
                "COALESCE(habitat_source,''),manual_elevation_msl_m,"+
                "elevation_msl_m,COALESCE(elevation_source,''),"+
                "vertical_uncertainty_m,COALESCE(updated_utc,'') "+
                "FROM station_environment ORDER BY updated_utc DESC LIMIT 35",null)){
            int seen=0;
            while(c.moveToNext()){
                String habitat=value(c,1,"Not assessed");
                String source=value(c,2,"Habitat source not recorded");
                String elevation="Elevation not available";
                if(!c.isNull(4)){
                    elevation=String.format(Locale.US,"%.1f m MSL",c.getDouble(4));
                }else if(!c.isNull(3)){
                    elevation=String.format(Locale.US,
                        "%.1f m MSL (manual)",c.getDouble(3));
                }
                String uncertainty=c.isNull(6)?"Accuracy not recorded":
                    String.format(Locale.US,"Vertical uncertainty ±%.1f m",c.getDouble(6));
                items.addView(FieldVisuals.context(this,
                    "Station "+value(c,0,"Unassigned")+"\nHabitat: "+habitat+
                    "\nReference: "+source+"\n"+elevation+
                    "\n"+uncertainty+"\nRecorded "+value(c,7,"time unavailable")));
                seen++;
            }
            if(seen==0)items.addView(label(
                "No per-visit environmental measurements are saved yet.",14));
        }catch(Exception err){items.addView(label(
            "Environmental visit records unavailable.",14));}
        TextView disclaimer=label("A habitat label or elevation is not evidence "+
            "that a species occurs at this station. More environmental "+
            "measurements and validated offline map layers are planned.",12);
        disclaimer.setTextColor(UiKit.MUTED);items.addView(disclaimer);
    }
    @Override protected void onDestroy(){helper.close();super.onDestroy();}
}