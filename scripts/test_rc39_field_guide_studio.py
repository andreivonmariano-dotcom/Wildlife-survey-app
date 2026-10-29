"""RC39: real offline gallery search and field desk usability without data/schema changes."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

def read(name):return (JAVA/name).read_text(encoding="utf-8")

class FieldGuideStudioQA(unittest.TestCase):
    def test_search_filters_only_existing_offline_references(self):
        cards=read("LikasDiscoveryActivity.java")
        for token in (
            'private boolean matches(JSONObject item,boolean isHabitat)',
            'item.optString("scientificName")',
            'item.optString("commonName")',
            'item.optString("overview")',
            'searchQuery.trim().toLowerCase(Locale.ROOT)',
            'return haystack.contains(query)',
            '"Birds","Amphibians","Reptiles","Mammals"',
            '"Terrestrial","Edaphic","Aquatic","Coastal","Human-modified"',
            'searchHandler.postDelayed(searchRefresh,180)',
            'searchHandler.removeCallbacks(searchRefresh)',
            'searchInput.setText("")',
            'searchQuery=saved.getString("searchQuery","")',
            'out.putString("searchQuery",searchQuery)',
            'out.putString("speciesGroup",speciesGroup)',
            'out.putString("habitatGroup",habitatGroup)',
            'if(animate)UiMotion.enter(this,content);',
            'emptyState()',
            '"No matches"',
            '"Full Philippine species checklist"',
            '"All habitats"',
        ):self.assertIn(token,cards,token)
        self.assertIn("if(status==null)return 0;",cards)
        self.assertIn('return Math.max(categoryLevel(status.optJSONObject("iucn"))',cards)
        self.assertIn('categoryLevel(status.optJSONObject("denr"))',cards)
        for forbidden in ("getWritableDatabase(","execSQL(","deleteDatabase(","new URL("):
            self.assertNotIn(forbidden,cards)

    def test_distinct_cards_keep_study_sources_and_unknowns(self):
        c=read("LikasDiscoveryActivity.java")
        art=read("LikasPortalUi.java")
        for glyph in ('"bird"','"frog"','"snake"','"mammal"',
                      '"forest"','"karst"','"wetland"','"coast"','"fields"'):
            self.assertIn(glyph,c)
        for glyph in ('case "snake":','case "forest":','case "karst":',
                      'case "wetland":','case "coast":','case "fields":'):
            self.assertIn(glyph,art)
        self.assertIn('TextView missing=note("Status unverified"',c)
        self.assertIn('putExtra("taxon_key",entry.optString("sourceTaxonKey"))',c)
        self.assertIn('putExtra("habitat_id",profile.optString("id"))',c)
        self.assertNotIn('"Priority species"',c)
        self.assertNotIn('rarityLevel(',c)

    def test_active_survey_and_setup_method_are_safe_and_readable(self):
        home=read("WelcomeActivity.java")
        portal=read("LikasPortalUi.java")
        self.assertIn("LikasPortalUi.session(this,",home)
        self.assertIn("Resume survey in progress",home)
        self.assertIn('String unfinished=activeSurvey()',home)
        self.assertIn('"field_tools_open"',home)
        self.assertIn('toolsToggle.setOnClickListener(v->{',home)
        for dest in ("SitesActivity.class","HistoryActivity.class",
                     "DashboardActivity.class","FieldProtocolsActivity.class",
                     "StudyProtocolsActivity.class","TrapProtocolActivity.class"):
            self.assertIn(dest,home)
        self.assertIn("private void updateMethodSelection(int checked)",home)
        self.assertIn("methodHint.setText(choices[choice].description)",home)
        self.assertIn("refreshStudyChoices();",home)
        self.assertIn('setupStartButton.setOnClickListener(v->startSelected())',home)
        self.assertIn('"Survey in progress"',portal)
        self.assertNotIn("DataManagementActivity.class",home)
        self.assertNotIn("FieldBackupActivity.class",home)

    def test_install_identity_schema_and_source_pack_stable(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 101",gradle)
        self.assertIn("versionName '0.27.3-rc41-field-readiness-preview'",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        self.assertIn("super(context,NAME,null,27)",read("FieldDb.java"))
        self.assertIn('rc34_conservation_card_status_v1.json',read("LikasDiscoveryActivity.java"))
        self.assertIn('rc18_unified_96_priority_accounts_v1.json',read("LikasDiscoveryActivity.java"))

if __name__=="__main__":unittest.main()
