package com.paymentgateway.controller;

import com.paymentgateway.bean.Order;
import com.paymentgateway.service.PayPalService;
import com.paymentgateway.service.impl.PayPalServiceImplementation;
import com.paymentgateway.util.Constants;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
public class PayPalController {

    @Autowired
    PayPalServiceImplementation payPalService ;



    @GetMapping("/")
    public String home(){
        return "home";
    }

    @GetMapping("/pay")
    public String payment(@ModelAttribute("order") Order order) {
        try {
            Payment payment = payPalService.createPayment(order.getPrice(), order.getCurrency(), order.getMethod(),order.getIntent(), order.getDescription(), Constants.BASE_URL+Constants.CANCEL_URL, Constants.BASE_URL+Constants.SUCCESS_URL);

            for(Links link: payment.getLinks()){
                if(link.getRel().equals("approval_url")){
                    return "redirect:"+link.getHref();
                }
            }
        } catch (PayPalRESTException e) {
            log.error(" Unable to carry out payment");
        }
        return "redirect:/";
    }
    @GetMapping(value = Constants.CANCEL_URL)
    public String cancelPay(){
        return "cancel";

    }

    @GetMapping(value = Constants.SUCCESS_URL)
    public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId){
        try{
            Payment payment = payPalService.executePayment(paymentId, payerId);

            if(payment.getState().equals("approved")){
                System.out.println(payment.toJSON());
                return "success";
            }

        }catch (PayPalRESTException e){
            System.out.println(e.getMessage());
        }
        return "redirect:/";
    }

}
