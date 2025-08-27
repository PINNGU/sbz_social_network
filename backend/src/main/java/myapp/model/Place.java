package myapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Place {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;
    @Column(name = "country")
    private String country;
    @Column(name = "town")  
    private String town;
    @Column(name = "description")
    private String description;
    @Column(name = "hashtag")
    private String hashtag;

}
