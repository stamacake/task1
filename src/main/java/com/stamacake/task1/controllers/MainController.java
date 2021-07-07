package com.stamacake.task1.controllers;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@Getter
@Setter
class UrlForm {
    private String url;
}

@RestController
@Slf4j
public class MainController {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS \"urls\" ( id serial primary key, url varchar(255) not null ); ");
    }

    private static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    public static final int BASE = alphabet.length();


    public static String idToShort(long id) {
        id -= 1;
        if (id == 0) {
            return "A";
        }
        StringBuilder str = new StringBuilder();

        while (id > 0) {
            str.append(alphabet.charAt((int) (id % BASE)));
            id /= BASE;
        }
        return str.reverse().toString();
    }

    public static long shortToId(String shortUrl) {
        long id = 0;
        for (int i = shortUrl.length() - 1, j = 0; i > -1; i--, j++) {
            id += Math.pow(BASE, j) * alphabet.indexOf(shortUrl.charAt(i));
        }

        return id + 1;
    }

    private String getUrlById(long id) {
        return jdbcTemplate.queryForObject("SELECT url FROM urls WHERE ID = \'" + id + "\'", String.class);
    }

    private Long getIdByUrl(String url) {
        return jdbcTemplate.queryForObject("SELECT id FROM urls WHERE url = \'" + url + "\'", Long.class);
    }

    @PostMapping(path = "/short")
    @ResponseBody
    public ResponseEntity<?> urlShort(@RequestBody UrlForm url) {
        if (url.getUrl() == null) {
            return (ResponseEntity.status(HttpStatus.BAD_REQUEST).body("enter url\n"));
        } else log.info("POST /short url: " + url.getUrl());
        Long id = -1L;
        // проверяем, есть ли в базе
        try {
            id = getIdByUrl(url.getUrl());
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        // если нашли
        if (id != -1) {
            UrlForm urlForm = new UrlForm();
            urlForm.setUrl(idToShort(getIdByUrl(url.getUrl())));
            return new ResponseEntity<>(urlForm, HttpStatus.OK);
        }

        // не нашли -> добавляем
        jdbcTemplate.execute("insert into urls (url) values ( \'" + url.getUrl() + "\');");
        log.info(url.getUrl());
        UrlForm urlForm = new UrlForm();
        urlForm.setUrl(idToShort(getIdByUrl(url.getUrl())));
        return new ResponseEntity<>(urlForm, HttpStatus.OK);
    }

    @PostMapping(path = "/long")
    public ResponseEntity<?> urlLong(@RequestBody UrlForm shortUrl) {
        if (shortUrl.getUrl() == null) {
            return (ResponseEntity.status(HttpStatus.BAD_REQUEST).body("enter url\n"));
        } else log.info("POST /long url: " + shortUrl.getUrl());

        Long id = shortToId(shortUrl.getUrl());
        try {
            UrlForm urlForm = new UrlForm();
            urlForm.setUrl(getUrlById(id));
            return new ResponseEntity<Object>(urlForm, HttpStatus.OK);
        } catch (Exception e) {
            log.info(e.getMessage());
            log.info("url not found");
            return new ResponseEntity<>("short url not found\n", HttpStatus.OK);
        }

    }
}
