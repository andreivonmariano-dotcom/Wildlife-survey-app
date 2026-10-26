package org.phwildlife.survey;
import java.util.*;
public final class Taxa {
    private Taxa(){}
    // Verified example taxa; NOT a complete national checklist. Names can vary by taxonomic authority.
    public static final String[] BIRDS = {
        "Philippine Eagle|Pithecophaga jefferyi",
        "Philippine Serpent-Eagle|Spilornis holospilus",
        "Philippine Hawk-Eagle|Nisaetus philippensis",
        "Mindanao Hawk-Eagle|Nisaetus pinskeri",
        "Philippine Scops-Owl|Otus megalotis",
        "Mindanao Scops-Owl|Otus mirus",
        "Philippine Eagle-Owl|Bubo philippensis",
        "Philippine Frogmouth|Batrachostomus septimus",
        "Philippine Trogon|Harpactes ardens",
        "Philippine Pitta|Erythropitta erythrogaster",
        "Mindanao Tarictic Hornbill|Penelopides affinis",
        "Visayan Hornbill|Penelopides panini",
        "Luzon Hornbill|Penelopides manillae",
        "Rufous Hornbill|Buceros hydrocorax",
        "Writhed Hornbill|Rhabdotorrhinus leucocephalus",
        "Palawan Hornbill|Anthracoceros marchei",
        "Philippine Duck|Anas luzonica",
        "Philippine Megapode|Megapodius cumingii",
        "Philippine Cockatoo|Cacatua haematuropygia",
        "Philippine Hanging-Parrot|Loriculus philippensis",
        "Blue-naped Parrot|Tanygnathus lucionensis",
        "Philippine Cuckoo-Dove|Macropygia tenuirostris",
        "Spotted Imperial-Pigeon|Ducula carola",
        "Pink-bellied Imperial-Pigeon|Ducula poliocephala",
        "Pied Imperial-Pigeon|Ducula bicolor",
        "White-eared Brown-Dove|Phapitreron leucotis",
        "Luzon Bleeding-heart|Gallicolumba luzonica",
        "Mindanao Bleeding-heart|Gallicolumba crinigera",
        "Philippine Coucal|Centropus viridis",
        "Greater Coucal|Centropus sinensis",
        "Philippine Nightjar|Caprimulgus manillensis",
        "Philippine Fairy-bluebird|Irena cyanogastra",
        "Philippine Bulbul|Hypsipetes philippinus",
        "Yellow-vented Bulbul|Pycnonotus goiavier",
        "Philippine Leaf Warbler|Phylloscopus olivaceus",
        "Lemon-throated Leaf Warbler|Phylloscopus cebuensis",
        "Sulphur-billed Nuthatch|Sitta oenochlamys",
        "Coleto|Sarcops calvus",
        "Apo Myna|Goodfellowia miranda",
        "Asian Glossy Starling|Aplonis panayensis",
        "Eurasian Tree Sparrow|Passer montanus",
        "Zebra Dove|Geopelia striata",
        "Spotted Dove|Spilopelia chinensis",
        "Red Collared-Dove|Streptopelia tranquebarica",
        "White-breasted Woodswallow|Artamus leucorynchus",
        "Olive-backed Sunbird|Cinnyris jugularis",
        "Brown Shrike|Lanius cristatus",
        "Barn Swallow|Hirundo rustica",
        "Pacific Swallow|Hirundo tahitica",
        "White-breasted Waterhen|Amaurornis phoenicurus",
        "Barred Rail|Gallirallus torquatus",
        "Plain Bush-hen|Amaurornis olivacea",
        "Philippine Swamphen|Porphyrio pulverulentus",
        "Collared Kingfisher|Todiramphus chloris",
        "Common Kingfisher|Alcedo atthis",
        "Stork-billed Kingfisher|Pelargopsis capensis",
        "Ruddy Kingfisher|Halcyon coromanda",
        "Blue-tailed Bee-eater|Merops philippinus",
        "Great Egret|Ardea alba",
        "Little Egret|Egretta garzetta",
        "Eastern Cattle-Egret|Ardea coromanda",
        "Black-crowned Night Heron|Nycticorax nycticorax",
        "Pacific Golden-Plover|Pluvialis fulva",
        "Whiskered Tern|Chlidonias hybrida",
        "Philippine Green-Pigeon|Treron axillaris",
        "Philippine Collared-Dove|Streptopelia dusumieri",
        "Philippine Cuckooshrike|Coracina striata",
        "Philippine Drongo-Cuckoo|Surniculus velutinus",
        "Visayan Tailorbird|Orthotomus castaneiceps",
        "Brown Tit-Babbler|Macronus striaticeps",
        "Striated Wren-Babbler|Ptilocichla mindanensis",
        "Mindanao Plumed-Warbler|Micromacronus sordidus",
        "Leyte Plumed-Warbler|Micromacronus leytensis",
        "Philippine Pygmy Woodpecker|Yungipicus maculatus",
        "Common Hill Myna|Gracula religiosa",
        "Black-winged Stilt|Himantopus himantopus",
        "Little Ringed Plover|Thinornis dubius",
        "Oriental Reed Warbler|Acrocephalus orientalis",
        "Zitting Cisticola|Cisticola juncidis",
        "Ashy Thrush|Geokichla cinerea",
        "Mindanao Island-Thrush|Turdus nigrorum",
        "Mindoro Island-Thrush|Turdus mindorensis"
    };
    public static final String[] REPTILES = {
        "Philippine Sailfin Lizard|Hydrosaurus pustulatus",
        "Philippine Crocodile|Crocodylus mindorensis",
        "Saltwater Crocodile|Crocodylus porosus",
        "Tokay Gecko|Gekko gecko",
        "Reticulated Python|Malayopython reticulatus",
        "Common Sun Skink|Eutropis multifasciata",
        "Monitor Lizard|Varanus sp."
    };
    public static final String[] AMPHIBIANS = {
        "Asian Common Toad|Duttaphrynus melanostictus",
        "Common Tree Frog|Polypedates leucomystax",
        "Crab-eating Frog|Fejervarya cancrivora",
        "Fanged Frog|Limnonectes sp.",
        "Stream Frog|Sanguirana sp.",
        "Tree Frog|Rhacophorus sp."
    };
    public static final String[] MAMMALS = {
        "Philippine Tarsier|Carlito syrichta",
        "Philippine Deer|Rusa marianna",
        "Philippine Warty Pig|Sus philippensis",
        "Visayan Warty Pig|Sus cebifrons",
        "Philippine Flying Lemur|Cynocephalus volans",
        "Long-tailed Macaque|Macaca fascicularis",
        "Large Flying Fox|Pteropus vampyrus",
        "Giant Golden-crowned Flying Fox|Acerodon jubatus",
        "Common Palm Civet|Paradoxurus hermaphroditus"
    };
    public static final String[] GROUPS={"Birds","Reptiles","Amphibians","Mammals"};
    public static String[] forGroup(String group) {
        switch(group){case "Birds":return BIRDS;case "Reptiles":return REPTILES;case "Amphibians":return AMPHIBIANS;default:return MAMMALS;}
    }
    public static String[] sortedLabels(String group) {
        String[] a=forGroup(group), b=new String[a.length];
        for(int i=0;i<a.length;i++){String[] p=a[i].split("\\|",2);b[i]=p[0]+" · "+p[1];}
        Arrays.sort(b,String.CASE_INSENSITIVE_ORDER);return b;
    }
    public static String[] identify(String label) {
        int i=label.indexOf(" · ");
        if(i>0)return new String[]{label.substring(0,i),label.substring(i+3)};
        for(String[] pool:new String[][]{BIRDS,REPTILES,AMPHIBIANS,MAMMALS})
            for(String s:pool){String[] p=s.split("\\|",2);
                if(p[0].equalsIgnoreCase(label)||p[1].equalsIgnoreCase(label))return p;}
        if(label.matches("[A-Z][a-z]+ (?:[a-z-]+|sp\\.)"))return new String[]{"",label};
        return new String[]{label,""};
    }
}
