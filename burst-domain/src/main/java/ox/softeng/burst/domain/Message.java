package ox.softeng.burst.domain;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema="Report")
@NamedQueries({
                      @NamedQuery(name="Message.MatchedMessages",
                                  query="select distinct m from Message m "
                                        + "where "
                                        + "m.dateTimeReceived < :dateNow "
                                        + "and m.dateTimeReceived >= :lastSentDate "
                                        + "and m.severityNumber >= :severity ")
              })
public class Message implements Serializable{


    private static final long serialVersionUID = 1L;
    protected OffsetDateTime dateTimeCreated;
    protected OffsetDateTime dateTimeReceived;
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    protected Long id = null;
    @Column(length = 10485760)
    protected String message;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "message")
    protected List<Metadata> metadata;
    @Enumerated(EnumType.STRING)
    protected SeverityEnum severity;
    protected int severityNumber;
    protected String source;
    protected String title;
    @Fetch(FetchMode.JOIN)
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(schema="Report")
    protected List<String> topics;

    public Message()
    {
        topics = new ArrayList<>();
        metadata = new ArrayList<>();
    }

    public Message(String source, String message, SeverityEnum severity, OffsetDateTime dateTimeCreated, String title)
    {
        dateTimeReceived = OffsetDateTime.now(ZoneId.of("UTC"));
        this.source = source;
        this.message = message;
        this.severity = severity;
        this.dateTimeCreated = dateTimeCreated;
        this.title = title;
        topics = new ArrayList<>();
        metadata = new ArrayList<>();
    }

    public void addMetadata(String key, String value)
    {
        metadata.add(new Metadata(key, value, this));
    }

    public void addTopic(String topic)
    {
        topics.add(topic);
    }

    public OffsetDateTime getDateTimeCreated() {
        return dateTimeCreated;
    }

    public void setDateTimeCreated(OffsetDateTime dateCreated) {
        this.dateTimeCreated = dateCreated;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<Metadata> metadata) {
        this.metadata = metadata;
    }

    public SeverityEnum getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityEnum severity) {
        this.severity = severity;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    @PrePersist
    public void updateSeverityNumber() {
        if(severity != null)
        {
        	this.severityNumber = severity.ordinal();
        }
    }
}
