package org.phwildlife.survey;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * APA 7 author-date display for source-backed field-guide claims.
 * Never guess missing authors, article titles, dates, page ranges or DOIs.
 * Full APA reference entries require verified bibliographic metadata.
 */
public final class ResearchCitationFormatter {
    private ResearchCitationFormatter(){}
    private static final Pattern AUTHOR_YEAR =
        Pattern.compile("^(.+?)\\s*\\((18\\d{2}|19\\d{2}|20\\d{2})([a-z]?)\\)");
    private static final Pattern YEAR_SUFFIX =
        Pattern.compile("^(18\\d{2}|19\\d{2}|20\\d{2})([a-z]?)$");

    private static String clean(String s) {
        return s==null?"":s.trim().replaceAll("\\s+"," ");
    }

    /** Only publication dates recorded in metadata or a literal author-year label count. */
    public static String yearLabel(int year,String source) {
        if(year>=1800&&year<=2100)return String.valueOf(year);
        Matcher m=AUTHOR_YEAR.matcher(clean(source));
        return m.find()?m.group(2)+m.group(3):"n.d.";
    }

    public static String authorLabel(String source) {
        String raw=clean(source);
        if(raw.isEmpty())return "";
        Matcher m=AUTHOR_YEAR.matcher(raw);
        if(m.find())return m.group(1).trim();
        int delimiter=raw.indexOf(" · ");
        if(delimiter>=0)raw=raw.substring(0,delimiter).trim();
        int comma=raw.indexOf(", ");
        if(comma>=0)raw=raw.substring(0,comma).trim();
        // A work title without an identifiable author stays title-led.
        return raw;
    }

    public static String inText(String source,int year) {
        String author=authorLabel(source);
        if(author.isEmpty())return "(Source metadata incomplete)";
        return "("+author+", "+yearLabel(year,source)+")";
    }

    /**
     * Source records may lack an original article title or full author list.
     * Keep their verbatim citation label and URL, with a clear incomplete-data note.
     */
    public static String sourceRecord(String source,int year,String url) {
        String original=clean(source);
        String author=authorLabel(source);
        String date=yearLabel(year,source);
        StringBuilder out=new StringBuilder();
        if(author.isEmpty())out.append("[Author not recorded]");
        else out.append(author);
        out.append(". (").append(date).append("). ");
        String work=original;
        Matcher m=AUTHOR_YEAR.matcher(original);
        if(m.find()){
            work=original.substring(m.end()).trim();
            if(work.startsWith(","))work=work.substring(1).trim();
        }else if(original.startsWith(author+" · ")){
            work=original.substring(author.length()+3);
        }else if(original.equals(author)){
            work="";
        }
        if(work.isEmpty())out.append("[Title not recorded].");
        else out.append(work.endsWith(".")?work:work+".");
        String link=clean(url);
        if(link.startsWith("https://"))out.append("\n").append(link);
        out.append("\nBibliographic metadata incomplete: verify the full author list and work title before citing in a manuscript.");
        return out.toString();
    }

    /** Group-authored web/report references with explicit titles. */
    public static String groupReference(String organization,String title,int year,String url) {
        String name=clean(organization),work=clean(title);
        StringBuilder out=new StringBuilder();
        out.append(name.isEmpty()?"[Author not recorded]":name);
        out.append(". (").append(year>=1800&&year<=2100?year:"n.d.").append("). ");
        out.append(work.isEmpty()?"[Title not recorded]":work);
        if(!work.endsWith("."))out.append(".");
        if(clean(url).startsWith("https://"))out.append("\n").append(clean(url));
        return out.toString();
    }

    /** Reference list metadata without a work title or full authors is marked as partial. */
    public static String publicationReference(String authors,int year,String articleTitle,
            String journal,String doi,String url,String source) {
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
