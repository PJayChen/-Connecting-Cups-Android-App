package com.mchp.android.PIC32_BTSK;

/**
 * Created by pjay on 2016/1/21.
 */
public class SimilarTemplate implements Comparable<SimilarTemplate>{

    private String templateName;
    private int similarity;


    public SimilarTemplate(String templateName, int similarity) {
        super();
        this.templateName = templateName;
        this.similarity = similarity;
    }


    @Override
    public int compareTo(SimilarTemplate similarityT) {
//        return Integer.compare(this.similarity, similarityT.similarity);
        return Integer.valueOf(this.similarity).compareTo(Integer.valueOf(similarityT.similarity));
    }


    public String getTemplateName() {
        return templateName;
    }


    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }


    public int getSimilarity() {
        return similarity;
    }


    public void setSimilarity(int similarity) {
        this.similarity = similarity;
    }

}
