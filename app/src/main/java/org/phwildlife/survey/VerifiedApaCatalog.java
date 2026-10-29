package org.phwildlife.survey;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Hand-checked APA 7 records from journal, publisher or indexing pages.
 * Exact source URLs are keys: no unverified metadata guesses and no online requests.
 * Expand only after checking original publication metadata and attribution.
 */
public final class VerifiedApaCatalog {
    private VerifiedApaCatalog(){}
    private static final class Record {
        final String inText,reference,journal;
        Record(String inText,String reference,String journal){
            this.inText=inText;this.reference=reference;this.journal=journal;
        }
    }
    private static final Map<String,Record> ENTRIES;
    static {
        Map<String,Record> m=new HashMap<>();
        m.put("https://doi.org/10.1017/S0959270926100586",new Record("(Sutton et al., 2026)",
            "Sutton, L. J., Ibañez, J. C., Salvador, D. I., Tirona, A. V. M. C., Opiso, G. S., Senarillos, T. L. P., Suetos, K. J., Taraya, R. L., & McClure, C. J. W. (2026). Directing conservation action for the Critically Endangered Philippine Eagle Pithecophaga jefferyi to mitigate mining impacts and maximise indigenous land management. Bird Conservation International, 36, Article e33. https://doi.org/10.1017/S0959270926100586","Bird Conservation International"));
        m.put("https://doi.org/10.1676/01-054",new Record("(Ibañez et al., 2003)",
            "Ibañez, J. C., Miranda, H. C., Jr., Balaquit-Ibañez, G., Afan, D. S., & Kennedy, R. S. (2003). Notes on the breeding behavior of a Philippine Eagle pair at Mount Sinaka, Central Mindanao. The Wilson Bulletin, 115(3), 333–336. https://doi.org/10.1676/01-054","The Wilson Bulletin"));
        m.put("https://doi.org/10.7717/peerj.18694",new Record("(Miñarro et al., 2025)",
            "Miñarro, M., Burrowes, P., Lansac, C., Sánchez-Montes, G., Afuang, L. E., & De la Riva, I. (2025). Demography and homing behavior in the poorly-known Philippine flat-headed frog Barbourula busuangensis (Anura: Bombinatoridae). PeerJ, 13, Article e18694. https://doi.org/10.7717/peerj.18694","PeerJ"));
        m.put("https://doi.org/10.11609/jott.7261.14.3.20713-20720",new Record("(Reintar et al., 2022)",
            "Reintar, A. R. T., Paguntalan, L. J., Jakosalem, P. G. C., Quidet, A. C. D., Warguez, D. A., & Peñaranda, E. (2022). Habitat preference and population density of threatened Visayan hornbills Penelopides panini and Rhabdotorrhinus waldeni in the Philippines. Journal of Threatened Taxa, 14(3), 20713–20720. https://doi.org/10.11609/jott.7261.14.3.20713-20720","Journal of Threatened Taxa"));
        m.put("https://doi.org/10.3897/zookeys.624.9814",new Record("(Sanguila et al., 2016)",
            "Sanguila, M. B., Cobb, K. A., Siler, C. D., Diesmos, A. C., Alcala, A. C., & Brown, R. M. (2016). The amphibians and reptiles of Mindanao Island, southern Philippines, II: The herpetofauna of northeast Mindanao and adjacent islands. ZooKeys, 624, 1–132. https://doi.org/10.3897/zookeys.624.9814","ZooKeys"));
        m.put("https://doi.org/10.3897/zookeys.1272.167304",new Record("(Sanguila et al., 2026)",
            "Sanguila, M. B., Labaja, A. C. O., Bernstein, J. M., & Brown, R. M. (2026). An updated herpetological inventory of the Agusan Marsh Wildlife Sanctuary, eastern Mindanao Island, Philippines. ZooKeys, 1272, 203–251. https://doi.org/10.3897/zookeys.1272.167304","ZooKeys"));
        m.put("https://doi.org/10.1038/s41598-026-76205-5",new Record("(Pascual et al., 2026)",
            "Pascual, F., Perdon, D. M., Tablizo, F., Lapid, C., Egana, J. M., Mantaring, R. J., Punayan, K., Araiza, S. M., Llames, J.-H., Abad, M. C., Gonzalez, J. C., Ibañez, J., & Palmes-Saloma, C. (2026). Genomic diversity, population structure, and relatedness of critically endangered Philippine eagles under conservation management. Scientific Reports. https://doi.org/10.1038/s41598-026-76205-5","Scientific Reports"));
        m.put("https://doi.org/10.1186/s40657-021-00303-3",new Record("(Mynott et al., 2021)",
            "Mynott, H. I., Lee, D. C., Santillan, R. A., Schwarz, C. J., Tacud, B., Fernandez, A. D., & Kerhoas, D. (2021). Population assessment and habitat associations of the Visayan Hornbill (Penelopides panini) in Northwest Panay, Philippines. Avian Research, 12, Article 67. https://doi.org/10.1186/s40657-021-00303-3","Avian Research"));
        m.put("https://doi.org/10.1098/rsbl.2010.0119",new Record("(Welton et al., 2010)",
            "Welton, L. J., Siler, C. D., Bennett, D., Diesmos, A., Duya, M. R., Dugay, R., Rico, E. L. B., van Weerd, M., & Brown, R. M. (2010). A spectacular new Philippine monitor lizard reveals a hidden biogeographic boundary and a novel flagship species for conservation. Biology Letters, 6(5), 654–658. https://doi.org/10.1098/rsbl.2010.0119","Biology Letters"));
        m.put("https://doi.org/10.59120/drj.v16i2.389",new Record("(Ramos & Nemenzo-Calica, 2025)",
            "Ramos, R. L., & Nemenzo-Calica, P. (2025). Flying fox species identification and habitat assessment on Luban Island, Philippines: Evidence of ecological shifts and conservation implications. Davao Research Journal, 16(2), 117–129. https://doi.org/10.59120/drj.v16i2.389","Davao Research Journal"));
        m.put("https://doi.org/10.1163/14219980-bja10081",new Record("(Bejar et al., 2026)",
            "Bejar, S. G. F., Amberong, A. G. T., Bonhomme, A., Devin, J., Pineau, P., Wojtasik, E., & Duya, M. R. M. (2026). Manoeuvring mangroves: A preliminary study on the use of a mangrove reserve by Philippine tarsiers (Carlito syrichta) on Siargao Island, Philippines. Folia Primatologica. https://doi.org/10.1163/14219980-bja10081","Folia Primatologica"));
        m.put("https://doi.org/10.26107/RBZ-2026-0015",new Record("(Meneses et al., 2026)",
            "Meneses, C. G., Pitogo, K. M. E., Decena, S. C. P., Supsup, C. E., & Brown, R. M. (2026). A new miniaturised Platymantis (Amphibia: Anura: Ceratobatrachidae) from Sibuyan and Tablas islands, Romblon Island Group, Philippines. Raffles Bulletin of Zoology, 74, 197–219. https://doi.org/10.26107/RBZ-2026-0015","Raffles Bulletin of Zoology"));
        m.put("https://doi.org/10.11646/zootaxa.2446.1.1",new Record("(Koch et al., 2010)",
            "Koch, A., Gaulke, M., & Böhme, W. (2010). Unravelling the underestimated diversity of Philippine water monitor lizards (Squamata: Varanus salvator complex), with the description of two new species and a new subspecies. Zootaxa, 2446(1), 1–54. https://doi.org/10.11646/zootaxa.2446.1.1","Zootaxa"));
        m.put("https://doi.org/10.11609/jott.10046.17.10.27551-27562",new Record("(Yangurin et al., 2025)",
            "Yangurin, I. J. A., Achondo, M. J. M., Raganas, A. F. M., Delima, A. G. D., Silvosa-Millado, C. S., Iñigo, D. J. B., Cabrera, S. M. E., Ollamina, S. M. M. R., Ibañez, J. C., & Gamalo, L. E. D. (2025). Fruit bat (Pteropodidae) composition and diversity in the montane forests of Mt. Kampalili, Davao de Oro, Philippines. Journal of Threatened Taxa, 17(10), 27551–27562. https://doi.org/10.11609/jott.10046.17.10.27551-27562","Journal of Threatened Taxa"));
        m.put("https://doi.org/10.11609/JoTT.o3748.6335-44",new Record("(Luczon et al., 2014)",
            "Luczon, A. U., Fontanilla, I. K. C., Ong, P. S., Basiao, Z. U., Sumaya, A. M. T., & Quilang, J. P. (2014). Genetic diversity of the Critically Endangered Philippine Eagle Pithecophaga jefferyi (Aves: Accipitridae) and notes on its conservation. Journal of Threatened Taxa, 6(10), 6335–6344. https://doi.org/10.11609/JoTT.o3748.6335-44","Journal of Threatened Taxa"));
        m.put("https://doi.org/10.1017/S0030605314000313",new Record("(Ishihara et al., 2015)",
            "Ishihara, S., Boyles, R. M., Matsubayashi, H., del Barrio, A. N., Cebrian, M. R., Ishida, A., Lapitan, R. M., Atabay, E. P., Cruz, L. C., & Kanai, Y. (2015). Long-term community-based monitoring of tamaraw Bubalus mindorensis on Mindoro Island, Philippines. Oryx, 49(2), 352–359. https://doi.org/10.1017/S0030605314000313","Oryx"));
        m.put("https://www.biodiversitylibrary.org/part/49110",new Record("(Esselstyn et al., 2004)",
            "Esselstyn, J. A., Widmann, P., & Heaney, L. R. (2004). The mammals of Palawan Island, Philippines. Proceedings of the Biological Society of Washington, 117(3), 271–302. https://www.biodiversitylibrary.org/part/49110","Proceedings of the Biological Society of Washington"));
        m.put("https://iucnhornbills.org/rufous-headed-hornbill/",new Record("(IUCN SSC Hornbill Specialist Group, n.d.)",
            "IUCN SSC Hornbill Specialist Group. (n.d.). Rufous-headed Hornbill. https://iucnhornbills.org/rufous-headed-hornbill/",""));
        m.put("https://iucnhornbills.org/northern-rufous-hornbill/",new Record("(IUCN SSC Hornbill Specialist Group, n.d.)",
            "IUCN SSC Hornbill Specialist Group. (n.d.). Northern Rufous Hornbill. https://iucnhornbills.org/northern-rufous-hornbill/",""));
        m.put("https://iucnhornbills.org/writhed-hornbill-2/",new Record("(IUCN SSC Hornbill Specialist Group, n.d.)",
            "IUCN SSC Hornbill Specialist Group. (n.d.). Writhed Hornbill. https://iucnhornbills.org/writhed-hornbill-2/",""));
        m.put("https://doi.org/10.2305/IUCN.UK.2020-3.RLTS.T22682522A184689600.en",new Record("(BirdLife International, 2020)",
            "BirdLife International. (2020). Rhabdotorrhinus leucocephalus. The IUCN Red List of Threatened Species 2020, e.T22682522A184689600. https://doi.org/10.2305/IUCN.UK.2020-3.RLTS.T22682522A184689600.en","The IUCN Red List of Threatened Species"));
        m.put("https://doi.org/10.2305/IUCN.UK.2016-3.RLTS.T22717516A94536769.en",new Record("(BirdLife International, 2016)",
            "BirdLife International. (2016). Dicaeum retrocinctum. The IUCN Red List of Threatened Species 2016, e.T22717516A94536769. https://doi.org/10.2305/IUCN.UK.2016-3.RLTS.T22717516A94536769.en","The IUCN Red List of Threatened Species"));
        m.put("https://nc.iucnredlist.org/redlist/content/attachment_files/2024-2_RL_Table_7.pdf",new Record("(IUCN, 2024)",
            "International Union for Conservation of Nature. (2024). [Table of species with IUCN Red List category changes, 2024-2] (Table 7). https://nc.iucnredlist.org/redlist/content/attachment_files/2024-2_RL_Table_7.pdf",""));
        m.put("https://doi.org/10.2305/IUCN.UK.2024-2.RLTS.T22683542A241691549.en",new Record("(BirdLife International, 2024)",
            "BirdLife International. (2024). Actenoides hombroni. The IUCN Red List of Threatened Species 2024, e.T22683542A241691549. https://doi.org/10.2305/IUCN.UK.2024-2.RLTS.T22683542A241691549.en","The IUCN Red List of Threatened Species"));
        m.put("https://bmb.gov.ph/downloads/WRD/WC/WC2020/stat_and_lists_of_wildlife/fauna/dao-2019-09.pdf",new Record("(Department of Environment and Natural Resources, 2019)",
            "Department of Environment and Natural Resources. (2019). Updated national list of threatened Philippine fauna and their categories (Administrative Order No. 2019-09). https://bmb.gov.ph/downloads/WRD/WC/WC2020/stat_and_lists_of_wildlife/fauna/dao-2019-09.pdf",""));
        m.put("https://doi.org/10.2307/1380399",new Record("(Musser & Gordon, 1981)",
            "Musser, G. G., & Gordon, L. K. (1981). A new species of Crateromys (Muridae) from the Philippines. Journal of Mammalogy, 62(3), 513–525. https://doi.org/10.2307/1380399","Journal of Mammalogy"));
        m.put("https://digitalcommons.usf.edu/auk/vol70/iss1/20/",new Record("(Manuel & Gilliard, 1953)",
            "Manuel, C. G., & Gilliard, E. T. (1953). A new name for a flowerpecker from the Philippines. The Auk, 70(1), 90. https://digitalcommons.usf.edu/auk/vol70/iss1/20/","The Auk"));
        ENTRIES=Collections.unmodifiableMap(m);
    }
    private static Record match(String url){
        if(url==null)return null;
        Record found=ENTRIES.get(url.trim());
        if(found!=null)return found;
        // Equivalent journal URLs are not silently matched to unrelated papers.
        return null;
    }
    public static String inText(String url){Record r=match(url);return r==null?null:r.inText;}
    public static String reference(String url){Record r=match(url);return r==null?null:r.reference;}
    public static String journal(String url){Record r=match(url);return r==null?null:r.journal;}
    public static int count(){return ENTRIES.size();}
}
