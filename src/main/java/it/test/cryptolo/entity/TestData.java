package it.test.cryptolo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_data")
public class TestData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "txt_col", length = 250, nullable = false)
    private String txtCol;

    @Column(name = "dt_col", nullable = false)
    private LocalDateTime dtCol;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTxtCol() {
        return txtCol;
    }

    public void setTxtCol(String txtCol) {
        this.txtCol = txtCol;
    }

    public LocalDateTime getDtCol() {
        return dtCol;
    }

    public void setDtCol(LocalDateTime dtCol) {
        this.dtCol = dtCol;
    }
}
