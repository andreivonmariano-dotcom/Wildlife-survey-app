package org.phwildlife.survey;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * APA 7 author-date presentation for evidence-backed LIKAS reference pages.
 * Original source strings remain stored unchanged in the source-pack JSON.
 * Missing bibliographic fields are not inferred from the date of access.
 */
public final class ResearchCitationFormatter {
    private ResearchCitationFormatter(){}
    private static final Pattern PAREN_YEAR=Pattern.compile(
        "^(.+?)\\s*\\((18\\d{2}|19\\d{2}|20\\d{2})([a-z]?)\\)");
    private static final Pattern BARE_YEAR=Pattern.compile(
        "^(.+?)\\s+(18\\d{2}|19\\d{2}|20\\d{2})([a-z]?)\\b");
    private static final Pattern LONG_DATE=Pattern.compile(
        "^(.+?)\\s*\\(\\d{1,2}\\s+[A-Za-z]+\\s+(18\\d{2}|19\\d{2}|20\\d{2})([a-z]?)\\)");
    private static String clean(String s){
        return s==null?"":s.trim().replaceAll("\\s+"," ");
    }
    private static Matcher yearMatch(String s){
        Matcher a=PAREN_YEAR.matcher(clean(s));
        if(a.find())return a;
        Matcher d=LONG_DATE.matcher(clean(s));
        if(d.find())return d;
        Matcher b=BARE_YEAR.matcher(clean(s));
        return b.find()?b:null;
    }
    public static String yearLabel(int year,String source){
        if(year>=1800&&year<=2100)return String.valueOf(year);
        Matcher found=yearMatch(source);
        return found==null?"n.d.":found.group(2)+found.group(3);
    }
    /** Preserves every author recorded in the source label for reference entries. */
    private static String fullAuthorLabel(String source){
        String raw=clean(source);
        if(raw.isEmpty())return "";
        Matcher match=yearMatch(raw);
        if(match!=null)return match.group(1).trim();
        int divider=raw.indexOf(" · ");
        if(divider>=0)raw=raw.substring(0,divider).trim();
        int comma=raw.indexOf(", ");
        if(comma<0)return raw;
        int second=raw.indexOf(", ",comma+2);
        if(second>comma&&raw.substring(comma+2,second).contains("&"))
            return raw.substring(0,second).trim();
        return raw.substring(0,comma).trim();
    }
    public static String authorLabel(String source){
        String authors=fullAuthorLabel(source);
        if(authors.isEmpty())return "";
        if(authors.matches(".*?,\\s*[^,]+?\\s*&\\s*[^,]+$"))
            return authors.substring(0,authors.indexOf(',')).trim()+" et al.";
        if(authors.matches(".*?,\\s*[^,]+,\\s*[^,]+$"))
            return authors.substring(0,authors.indexOf(',')).trim()+" et al.";
        return authors;
    }
    public static String inText(String source,int year){
        String author=authorLabel(source);
        if(author.isEmpty())return "(Source details unavailable)";
        return "("+author+", "+yearLabel(year,source)+")";
    }
    /** Exact verified DOI record takes priority over potentially abbreviated labels. */
    public static String inText(String source,int year,String url){
        String checked=VerifiedApaCatalog.inText(url);
        return checked==null?inText(source,year):checked;
    }
    public static String citeClaim(String claim,String source,int year,String url){
        String s=clean(claim);
        if(s.endsWith("."))s=s.substring(0,s.length()-1);
        return s+" "+inText(source,year,url)+".";
    }
    public static String citeClaim(String claim,String source,int year){
        String s=clean(claim);
        if(s.endsWith("."))s=s.substring(0,s.length()-1);
        return s+" "+inText(source,year)+".";
    }
    public static String sourceRecord(String source,int year,String url){
        String verified=VerifiedApaCatalog.reference(url);
        if(verified!=null)return verified;
        String original=clean(source),author=fullAuthorLabel(source);
        String date=yearLabel(year,source);
        Matcher match=yearMatch(original);
        String work="";
        if(match!=null){
            work=original.substring(match.end()).replaceFirst("^[,.:\\s]+","").trim();
        }else {
            int comma=original.indexOf(", ");
            int divider=original.indexOf(" · ");
            if(divider>=0)work=original.substring(divider+3).trim();
            else if(comma>=0)work=original.substring(comma+2).trim();
        }
        if(work.isEmpty())work="[Title not verified]";
        StringBuilder out=new StringBuilder();
        out.append(author.isEmpty()?"[Author not recorded]":author);
        if(!author.endsWith("."))out.append(".");
        out.append(" (").append(date).append("). ").append(work);
        if(!work.endsWith("."))out.append(".");
        String link=clean(url);
        if(link.startsWith("https://"))out.append("\n").append(link);
        return out.toString();
    }
    public static String groupReference(String organization,String title,int year,String url){
        String name=clean(organization),work=clean(title);
        StringBuilder out=new StringBuilder();
        out.append(name.isEmpty()?"[Author not recorded]":name);
        out.append(". (").append(year>=1800&&year<=2100?year:"n.d.").append("). ");
        out.append(work.isEmpty()?"[Title not recorded]":work);
        if(!work.endsWith("."))out.append(".");
        if(clean(url).startsWith("https://"))out.append("\n").append(clean(url));
        return out.toString();
    }
    /** Full APA reference when original metadata identifies all authors and the work title. */
    public static String publicationReference(String authors,int year,String articleTitle,
            String journal,String doi,String url,String source){
        String verified=VerifiedApaCatalog.reference(url);
        if(verified!=null)return verified;
        String name=clean(authors),title=clean(articleTitle),venue=clean(journal);
        if(name.isEmpty()||title.isEmpty()||name.contains("et al."))
            return sourceRecord(source,year,url);
        StringBuilder out=new StringBuilder(name).append(". (");
        out.append(year>=1800&&year<=2100?year:"n.d.").append("). ");
        out.append(title).append(title.endsWith(".")?"":".");
        if(!venue.isEmpty())out.append(" ").append(venue).append(".");
        String identifier=clean(doi);
        if(!identifier.isEmpty())out.append("\nhttps://doi.org/")
            .append(identifier.replace("https://doi.org/",""));
        else if(clean(url).startsWith("https://"))out.append("\n").append(clean(url));
        return out.toString();
    }
}
