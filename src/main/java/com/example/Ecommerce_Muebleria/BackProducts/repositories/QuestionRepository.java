package com.example.Ecommerce_Muebleria.BackProducts.repositories;


import com.example.Ecommerce_Muebleria.entities.products.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    Optional<Question> findQuestionById(Long id);
    public void delete(Question question);
    public Question save(Question question);

}
