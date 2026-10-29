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
