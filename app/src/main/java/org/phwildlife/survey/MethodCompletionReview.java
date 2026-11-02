package org.phwildlife.survey;

/**
 * Read-only, evidence-based *method-record* preview for the end-survey sheet.
 * This is not a replacement for SurveyClosePolicy, template checks, elapsed
 * effort, route/area confirmation or actual survey closure.
 */
public final class MethodCompletionReview {
    private MethodCompletionReview(){}
    public enum Kind { NONE, FIELD_EFFORT, DEVICES }
    public static final class Result {
        public final Kind kind;
        public final boolean methodRecordsReady;
        public final String headline, detail, actionLabel;
        Result(Kind kind,boolean ready,String headline,String detail,String actionLabel){
            this.kind=kind;
            this.methodRecordsReady=ready;
            this.headline=headline;
            this.detail=detail;
            this.actionLabel=actionLabel;
        }
    }
    public static Result assess(String method,int[] field,int[] devices){
        if(FieldProtocolPolicy.supported(method)){
            if(field==null||field.length<3)
                throw new IllegalArgumentException("Field effort counts required");
            int units=Math.max(0,field[0]);
            int missed=Math.max(0,field[1]);
            int invalid=Math.max(0,field[2]);
            boolean ready=FieldProtocolPolicy.canComplete(method,units,missed,invalid);
            String unitLabel="coverboard_search".equals(method)?"boards":"passes";
            String detail="Recorded "+unitLabel+": "+units+". ";
            if("coverboard_search".equals(method))
                detail+="Uninspected boards: "+missed+". ";
            else detail+="Invalid or incomplete effort entries: "+invalid+". ";
            detail+="Open the method-effort record to check source measurements.";
            return new Result(Kind.FIELD_EFFORT,ready,
                ready?"Method records ready for final checks":
                      "Method records need review",
                detail,"Review method effort");
        }
        if(TrapProtocolPolicy.supported(method)){
            if(devices==null||devices.length<4)
                throw new IllegalArgumentException("Device check counts required");
            int installed=Math.max(0,devices[0]);
            int unchecked=Math.max(0,devices[1]);
            int open=Math.max(0,devices[2]);
            int captures=Math.max(0,devices[3]);
            boolean ready=TrapProtocolPolicy.canComplete(installed,unchecked,open,captures);
            String detail="Deployments: "+installed+
                ". Without a recorded check: "+unchecked+
                ". Missing capture details: "+captures+
                ". Still deployed: "+open+
                " (retrieval is not required for every check).";
            return new Result(Kind.DEVICES,ready,
                ready?"Device checks ready for final checks":
                      "Device records need review",detail,"Review device checks");
        }
        return new Result(Kind.NONE,false,
            "Review survey effort and sampling units",
            "The final outcome still depends on the selected method, actual active effort, "+
            "station completion, route/area measurements, and study rules. "+
            "Recorded sightings alone do not establish complete sampling.",
            "");
    }
}
