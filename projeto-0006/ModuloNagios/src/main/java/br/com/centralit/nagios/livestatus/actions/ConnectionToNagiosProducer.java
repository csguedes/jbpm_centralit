/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.centralit.nagios.livestatus.actions;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 *
 * @author adenir
 */
@Named(value = "connectionToNagiosProducer")
@RequestScoped
public class ConnectionToNagiosProducer {
    private String message;
    
    @Resource(mappedName = "java:/jms/CitsmartEVM/nagiosEventResponse")
    private Queue queueRequest;
    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory queueRequestFactory;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    /**
     * Creates a new instance of MessageProducerBean
     */
    public ConnectionToNagiosProducer() {
    }
    
    public void send(){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
             sendJMSMessageToQueueRequest(message);
             System.out.println("Modulo Nagios: " + message);
        //     FacesMessage facesMessage = new FacesMessage("Message sent: " + message);
         //    facesMessage.setSeverity(FacesMessage.SEVERITY_INFO);
         //    facesContext.addMessage(null, facesMessage);
        } catch (JMSException jmse) {
             FacesMessage facesMessage = new FacesMessage("Message NOT sent: " + message);
             facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
            facesContext.addMessage(null, facesMessage);
        } 
    }

    private Message createJMSMessageForqueueRequest(Session session, Object messageData) throws JMSException {
        // TODO create and populate message to send
        TextMessage tm = session.createTextMessage();
        tm.setText(messageData.toString());
        return tm;
    }

    private void sendJMSMessageToQueueRequest(Object messageData) throws JMSException {
        Connection connection = null;
        Session session = null;
        try {
            connection = queueRequestFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(queueRequest);
            messageProducer.send(createJMSMessageForqueueRequest(session, messageData));
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Cannot close session", e);
                }
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}

