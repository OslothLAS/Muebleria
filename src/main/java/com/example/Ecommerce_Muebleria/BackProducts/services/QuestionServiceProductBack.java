package com.example.Ecommerce_Muebleria.BackProducts.services;


import com.example.Ecommerce_Muebleria.entities.products.Question;
import com.example.Ecommerce_Muebleria.BackProducts.repositories.QuestionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class QuestionServiceProductBack {

    @Autowired
    private QuestionRepository questionRepository;

    @Transactional
    public Question save(Question question) {
        return questionRepository.save(question);
    }

    public Optional<Question> findById(Long id) {return questionRepository.findQuestionById(id);}

    public void delete(Question question) {
        this.questionRepository.delete(question);
    }
}
