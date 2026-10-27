package org.phwildlife.survey;

import java.util.Locale;

/**
 * Transparent heuristic, NOT a statistical probability, occupancy estimate,
 * absence assertion, or published species distribution model.
 */
public final class HabitatLikelihood {
    private HabitatLikelihood(){}
    public static final String[] HABITATS={
        "Unassessed","Lowland forest","Montane forest","Mossy forest",
        "Secondary forest","Grassland","Agriculture / plantation",
        "Wetland","Riparian / stream","Coastal / mangrove","Urban / settlement"
    };
    public static final String[] CODES={
        "","lowland","montane","mossy","secondary","grassland",
        "agriculture","wetland","riparian","mangrove","urban"
    };
    public static int habitatIndex(String code){
        for(int i=0;i<CODES.length;i++)if(CODES[i].equals(code))return i;
        return 0;
    }
    public static String codeAt(int position){
        return CODES[Math.max(0,Math.min(CODES.length-1,position))];
    }
    public static final class Result{
        public final String label,explanation;
        public final int points;
        public final boolean habitatConflict,elevationConflict;
        private Result(String label,int points,boolean h,boolean e,String reason){
            this.label=label;this.points=points;
            habitatConflict=h;elevationConflict=e;explanation=reason;
        }
    }
    public static Result assess(int evidenceRank,String siteHabitat,Double siteElevation,
            Double elevationError,String reviewedHabitats,boolean strictHabitat,
            Double minElevation,Double maxElevation,boolean profileReviewed){
        int rank=Math.max(0,Math.min(2,evidenceRank));
        int points=rank==2?3:rank==1?2:0;
        boolean matchHabitat=false,conflictHabitat=false;
        boolean matchElev=false,conflictElev=false;
        String habitat=siteHabitat==null?"":siteHabitat.trim().toLowerCase(Locale.ROOT);
        String codes=reviewedHabitats==null?"":reviewedHabitats.toLowerCase(Locale.ROOT);
        if(profileReviewed&&!habitat.isEmpty()&&!codes.isEmpty()){
            String[] values=codes.split("\\|");
            for(String item:values)if(item.trim().equals(habitat))matchHabitat=true;
            if(matchHabitat)points+=2;
            else if(strictHabitat){conflictHabitat=true;points-=2;}
        }
        if(profileReviewed&&siteElevation!=null&&Double.isFinite(siteElevation)&&
                (minElevation!=null||maxElevation!=null)){
            double uncertainty=elevationError!=null&&Double.isFinite(elevationError)&&
                elevationError>=0?elevationError:100.0;
            double margin=Math.max(100.0,uncertainty*2.0);
            boolean high=maxElevation!=null&&Double.isFinite(maxElevation)&&
                siteElevation>maxElevation+margin;
            boolean low=minElevation!=null&&Double.isFinite(minElevation)&&
                siteElevation<minElevation-margin;
            if(high||low){conflictElev=true;points-=2;}
            else{
                // Near a published range boundary is uncertain, not a match.
                boolean withinMin=minElevation==null||siteElevation>=minElevation+uncertainty;
                boolean withinMax=maxElevation==null||siteElevation<=maxElevation-uncertainty;
                if(withinMin&&withinMax){matchElev=true;points+=1;}
            }
        }
        String label="Insufficient Data";
        if(conflictHabitat&&conflictElev)label="Unlikely";
        else if(rank>0&&points>=3)label="Likely";
        StringBuilder why=new StringBuilder();
        why.append(rank==2?"Documented province record":rank==1?
            "Documented island record":"Regional presence unassessed");
        why.append(" · ");
        if(!profileReviewed)why.append("No reviewed species habitat profile");
        else{
            why.append(matchHabitat?"Habitat matches":conflictHabitat?
                "Strict habitat conflict":"Habitat unknown/unassessed");
            why.append(" · ");
            why.append(matchElev?"Elevation matches":conflictElev?
                "Elevation well outside reviewed range":"Elevation unknown/uncertain");
        }
        why.append(". Screening only; not an occupancy probability.");
        return new Result(label,points,conflictHabitat,conflictElev,why.toString());
    }
}
