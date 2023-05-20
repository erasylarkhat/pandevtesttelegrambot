package io.proj3ct.SpringDemoBot.repository;

import io.proj3ct.SpringDemoBot.model.MessageModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<MessageModel, Long> {

}
