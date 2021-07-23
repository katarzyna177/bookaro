package pl.kate.bookaro.uploads.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.kate.bookaro.jpa.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Upload extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;
    private byte[] file;
    private String contentType;
    private String filename;
    @CreatedDate
    private LocalDateTime createdAt;

    public Upload(String filename, String contentType, byte[] file) {
        this.filename = filename;
        this.contentType = contentType;
        this.file = file;
    }
}
