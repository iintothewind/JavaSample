package sample.csv.bean

import com.google.common.base.MoreObjects
import com.google.common.base.Preconditions
import com.google.common.collect.ComparisonChain
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Test

import java.beans.BeanInfo
import java.beans.Introspector

public class Metadata implements Comparable<Metadata> {
    private static final DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("dd/MM/yyyy")
    String fileName
    String contentType
    int adviserNumber
    DateTime creationDate
    String documentTitle
    String firstName
    String lastName
    String nzBrand
    String nzDocumentCategory
    String policyNumber
    String productName
    String insert


    private static DateTime formatDate(String date) {
        Preconditions.checkNotNull(date)
        return dateTimeFormat.parseDateTime(date)
    }

    public static Metadata from(Map<String, String> map) {
        Preconditions.checkNotNull(map)
        def metaData = new Metadata()
        metaData.setFileName(map.get("filename"))
        metaData.setContentType(map.get("contentType"))
        metaData.setAdviserNumber(Integer.parseInt(map.get("AdviserNumber")))
        metaData.setCreationDate(formatDate(map.get("Creation_Date")))
        metaData.setDocumentTitle(map.get("DocumentTitle"))
        metaData.setFirstName(map.get("FirstName"))
        metaData.setLastName(map.get("LastName"))
        metaData.setNzBrand(map.get("NZ_Brand"))
        metaData.setNzDocumentCategory(map.get("NZ_DocumentCategory"))
        metaData.setPolicyNumber(map.get("PolicyNumber"))
        metaData.setProductName(map.get("ProductName"))
        metaData.setInsert(map.get("_Insert1"))
        return metaData;
    }

    @Override
    public int compareTo(Metadata that) {
        if (that == null)
            return -1;
        else {
            return ComparisonChain.start()
                    .compare(this.adviserNumber, that.adviserNumber)
                    .compare(this.fileName, that.fileName)
                    .compare(this.contentType, that.contentType)
                    .compare(this.creationDate, that.creationDate)
                    .compare(this.documentTitle, that.documentTitle)
                    .compare(this.firstName, that.firstName)
                    .compare(this.lastName, that.lastName)
                    .compare(this.nzBrand, that.nzBrand)
                    .compare(this.nzDocumentCategory, that.nzDocumentCategory)
                    .compare(this.policyNumber, that.policyNumber)
                    .compare(this.productName, that.productName)
                    .compare(this.insert, that.insert)
                    .result();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("filename", this.fileName)
                .add("contentType", this.contentType)
                .add("AdviserNumber", this.adviserNumber)
                .add("Creation_Date", this.creationDate.toString(dateTimeFormat))
                .add("DocumentTitle", this.documentTitle)
                .add("FirstName", this.firstName)
                .add("LastName", this.lastName)
                .add("NZ_Brand", this.nzBrand)
                .add("NZ_DocumentCategory", this.nzDocumentCategory)
                .add("PolicyNumber", this.policyNumber)
                .add("ProductName", this.productName)
                .add("_Insert1", this.insert)
                .toString()
    }

    @Test
    public void testBeans() {
        BeanInfo beanInfo = Introspector.getBeanInfo(this.class)
        //beanInfo.getPropertyDescriptors().each { println it }
        beanInfo.methodDescriptors.each {
            if (it.method.genericParameterTypes.find { it.typeName.contains("Metadata") } && it.name == 'compareTo') println it
        }
    }
}
