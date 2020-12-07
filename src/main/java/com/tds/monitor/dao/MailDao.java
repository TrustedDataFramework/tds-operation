package com.tds.monitor.dao;

import com.tds.monitor.model.Mail;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MailDao extends JpaRepository<Mail, Long> {

}
