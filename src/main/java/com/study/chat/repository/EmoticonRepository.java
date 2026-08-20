package com.study.chat.repository;

import com.study.chat.domain.Emoticon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmoticonRepository extends JpaRepository<Emoticon, Long> {
}
