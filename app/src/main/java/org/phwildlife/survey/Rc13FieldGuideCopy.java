package org.phwildlife.survey;

/** Editorial shortening for the on-screen field guide. Original evidence assets are intact. */
public final class Rc13FieldGuideCopy {
    private Rc13FieldGuideCopy(){}
    private static final String[] EDITORIAL_TAILS={
        "; habitat is context, not diagnostic proof",
        "; obtain clear images rather than identifying from a brief flush.",
        ". Female identification needs specialist comparison.",
        ". An island name is not confirmation at an individual point.",
        ". Records from other islands do not establish this species.",
        ". Historical island records do not establish a current occupied site.",
        ". Show the assessment year, not an undated category.",
        "; store original audio rather than guessing a species from an unreviewed recording.",
        "; a published record is not proof of local station presence",
        "; this is not a site occurrence claim.",
        ". This is a published range/habitat summary, not proof of occurrence at a LIKAS station.",
        "; survey-site occupancy is not established.",
        ". Voice and colors vary by sex and light, so record photography and exact detection type."
    };
    // Screen copy only: retain original verbatim claims and source links in JSON assets.
    private static final java.util.Map<String,String> EDITS=new java.util.HashMap<>();
    static {
        EDITS.put("Bat Conservation International describes distinctive gold-colored fur on the head of the giant golden-crowned flying fox.", "Distinctive golden fur covers the head of the giant golden-crowned flying fox.");
        EDITS.put("Published species notes report tree roosting, sometimes in colonies with large flying foxes.", "Roosts in trees, sometimes in mixed colonies with large flying foxes.");
        EDITS.put("The Bat Conservation International account notes forest loss, hunting and roost disturbance as central concerns.", "Forest loss, hunting and disturbance at roost sites are documented conservation threats.");
        EDITS.put("The published Rasa study describes old-growth mangrove and coastal forest as nesting and feeding habitat.", "On Rasa Island, nesting and feeding were documented in old-growth mangrove and coastal forest.");
        EDITS.put("The account describes dense forest understorey and forest-floor foraging, including some secondary and logged forest on Panay.", "Occurs in dense forest understorey and forages on the forest floor, including in some secondary and logged forests on Panay.");
        EDITS.put("The specialist description notes a rapid, noisy series of calls; store original audio rather than guessing a species from an unreviewed recording.", "Calls include rapid, noisy series.");
        EDITS.put("Prominent blue fleshy eye wattles, purple crown, grey nuchal collar and lilac underparts are described in the attributed field guide.", "Prominent blue eye wattles, a purple crown, grey nuchal collar and lilac underparts.");
        EDITS.put("A mainly terrestrial, day-active invertebrate-foraging pitta; the cited reference describes feeding in forest ground vegetation.", "Mainly terrestrial and active by day, feeding on invertebrates among forest-floor vegetation.");
        EDITS.put("The 2025 study describes B. busuangensis as a fully aquatic, nocturnal species.", "Fully aquatic and nocturnal in the sampled populations.");
        EDITS.put("A historical IUCN threatened amphibians account describes limestone karst forest and caves in forested lowlands.", "Historically documented from limestone-karst forest and caves in forested lowlands.");
        EDITS.put("Amphibian Species of the World reports primary and secondary forest on limestone outcrops on South Gigante Island.", "Recorded in primary and secondary forests on limestone outcrops on South Gigante Island.");
        EDITS.put("American Museum of Natural History describes limestone outcrops in primary and secondary forest on South Gigante Island.", "Limestone outcrops in primary and secondary forest on South Gigante Island.");
        EDITS.put("The source describes limestone karst areas of southern Negros, with published records in roughly 20–400 m elevations.", "Recorded in limestone-karst areas of southern Negros, at approximately 20–400 m elevation.");
        EDITS.put("The 1997 revision describes a pointed, sometimes knob-tipped snout and a distinct tympanum measuring about 25–42% of eye diameter in its examined series; unlike most other hazelae-group species, it lacks yellow-and-brown areolations in the groin and thighs.", "Examined specimens have a pointed, sometimes knob-tipped snout and a tympanum about 25–42% of eye diameter. Yellow-and-brown groin and thigh areolations are absent in the examined series, unlike most related hazelae-group species.");
        EDITS.put("The 1997 revision reports P. hazelae chiefly from arboreal ferns and Pandanus leaf axils in submontane and montane Negros forests, with a smaller set of observations from shrubs, stream rocks and lower-elevation dipterocarp forest.", "On Negros, most documented specimens occupied arboreal ferns and Pandanus leaf axils in submontane and montane forest. Smaller samples came from shrubs, stream rocks and lower dipterocarp forest.");
        EDITS.put("The northern Sierra Madre survey reports live Philippine crocodiles in freshwater rivers, creeks and ponds, and in coastal riverine habitats with saline conditions in parts of Isabela.", "In the northern Sierra Madre, Philippine crocodiles were observed in freshwater rivers, creeks, ponds and some saline coastal river habitats in Isabela.");
        EDITS.put("Smithsonian's species account describes females making mound or hole nests and both sexes guarding the nest; recorded clutches comprise seven to thirty eggs.", "Females build mound or hole nests. Both sexes guard nests, and documented clutches contain 7–30 eggs.");
        EDITS.put("The Agusan Marsh treatment describes enlarged toe lamellae and a laterally flattened dorsal sail with serrated edges.", "Enlarged toe lamellae and a laterally flattened dorsal sail with serrated edges.");
        EDITS.put("The comparative V. mabitang account describes V. olivaceus as greenish grey with darker crossbands.", "Greenish-grey dorsal coloration with darker crossbands in the published comparison.");
        EDITS.put("The original comparative diagnosis describes strongly keeled ventrals and strongly keeled tail scales, unlike the smoother comparison recorded for V. olivaceus.", "Ventral and tail scales are strongly keeled in the examined comparison, unlike the smoother scales documented for V. olivaceus.");
        EDITS.put("The original diagnosis describes contrasting black and golden-yellow dorsal coloration.", "Contrasting black and golden-yellow dorsal coloration.");
        EDITS.put("The database describes noticeably enlarged raised nuchal scales on the neck as a central characteristic.", "Distinctive enlarged, raised nuchal scales on the neck.");
        EDITS.put("The Zoological Society of London describes short, backward-pointing horns on both sexes and a dark brown to greyish coat with lighter markings in some animals.", "Both sexes have short, backward-pointing horns. The coat is dark brown to greyish, sometimes with lighter markings.");
        EDITS.put("The IUCN SSC Pangolin Specialist Group reports that this species has more dorsal scale rows than other Asian pangolins and smaller scales than Sunda pangolin.", "More dorsal scale rows than other Asian pangolins, with smaller scales than the Sunda pangolin.");
        EDITS.put("The specialist group describes more dorsal scale rows and smaller scales than the Sunda pangolin.", "More dorsal scale rows and smaller scales than the Sunda pangolin.");
        EDITS.put("The specialist account describes a shorter head-and-body to tail-length ratio relative to Sunda pangolin.", "A shorter head-and-body to tail-length ratio than the Sunda pangolin.");
        EDITS.put("The specialist account notes a distinctive snout band and relatively small facial warts.", "A distinctive snout band and relatively small facial warts.");
        EDITS.put("The 2020 research review describes a mostly nocturnal, solitary, partly tree-dwelling species that searches for ants and termites.", "Mostly nocturnal and solitary, with both ground and tree foraging for ants and termites.");
        EDITS.put("A long-term field monitoring study describes perennial grassland, secondary forest and bamboo patches across monitored sites in Mount Iglit–Baco.", "Monitored tamaraw sites in Mount Iglit–Baco include perennial grassland, secondary forest and bamboo patches.");
    }
    public static String fieldText(String value){
        if(value==null)return "";
        String shown=EDITS.getOrDefault(value.trim(),value.trim());
        for(String tail:EDITORIAL_TAILS){
            int at=shown.indexOf(tail);
            if(at>=0)shown=shown.substring(0,at).trim();
        }
        return shown;
    }
}
