package com.salgaki.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "estoques")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer quantidade = 0;

    @Column(name = "data_validade", nullable = false)
    private LocalDate dataValidade;

    @OneToMany(mappedBy = "estoque", cascade = CascadeType.ALL)
    private List<MovimentacaoEstoque> movimentacoes;
}