package se.kry.codetest.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ServiceEntity {
    private int id;
    private String name;
    private String url;
    private String status;
    private Date creationDate;

    public ServiceEntity(int id, String name, String url, String status, Date creationDate) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.status = status;
        this.creationDate = creationDate;
    }

    public ServiceEntity(String name, String url, String status) {
        this.name = name;
        this.url = url;
        this.status = status;
        this.creationDate = Calendar.getInstance().getTime();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getCreationDateISO8601() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        return df.format(creationDate);
    }

    public String getStatus() {
        return status;
    }
}
