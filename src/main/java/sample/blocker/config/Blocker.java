package sample.blocker.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Blocker {
  private String prefix;
  private int before;
  private int after;

  @JacksonXmlProperty(isAttribute = true)
  public String getPrefix() {
    return prefix;
  }

  @JacksonXmlProperty(isAttribute = true)
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  @JacksonXmlProperty(isAttribute = true)
  public int getBefore() {
    return before;
  }

  @JacksonXmlProperty(isAttribute = true)
  public void setBefore(int before) {
    this.before = before;
  }

  @JacksonXmlProperty(isAttribute = true)
  public int getAfter() {
    return after;
  }

  @JacksonXmlProperty(isAttribute = true)
  public void setAfter(int after) {
    this.after = after;
  }
}
