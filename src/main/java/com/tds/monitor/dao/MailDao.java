package com.tds.monitor.dao;

import com.tds.monitor.model.Mail;
import com.tds.monitor.model.Nodes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MailDao extends JpaRepository<Mail, Long> {

}
