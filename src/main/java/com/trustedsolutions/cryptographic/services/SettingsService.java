/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.services;

import com.core.cryptolib.components.SettingObject;
import com.core.cryptolib.components.Settings;
import com.trustedsolutions.cryptographic.model.Setting;
import com.trustedsolutions.cryptographic.repository.SettingRepository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SettingsService {

    @Autowired
    SettingRepository settingRepository;

    public int getSettingsCount() {
        try {

            List<Setting> stList = (List<Setting>) settingRepository.findAll();
            return stList.size();
        } catch (Exception ex) {
            return 0;
        }
    }

    @Async
    public void async(Settings settings) {

        Runnable task = () -> {
            try {
                List<Setting> stList = (List<Setting>) settingRepository.findAll();

                stList.forEach(item -> {
                    settings.getSettings().stream().filter(sub -> (item.getKey().equals(sub.getKey()))).map(sub -> {
                        item.setValue(sub.getValue());
                        return sub;
                    }).forEachOrdered(_item -> {
                        settingRepository.save(item);
                    });
                });

            } catch (Exception ex) {

            }
        };
        Thread thread = new Thread(task);
        thread.start();

    }

    public void sync(Settings settings) {

        List<Setting> stList = (List<Setting>) settingRepository.findAll();

        stList.forEach(item -> {
            settings.getSettings().stream().filter(sub -> (item.getKey().equals(sub.getKey()))).map(sub -> {
                item.setValue(sub.getValue());
                return sub;
            }).forEachOrdered(_item -> {
                settingRepository.save(item);
            });
        });

    }

    public Settings getAllSettings() {

        try {
            List<Setting> stList = (List<Setting>) settingRepository.findAll();

            Settings settings = new Settings();

            stList.forEach(item -> {
                settings.put(new SettingObject(
                        item.getKey(),
                        item.getValue()
                ));
            });

            return settings;

        } catch (Exception ex) {
            return new Settings();
        }

    }

    public Setting get(String key, String defaultValue) {

        Setting st = settingRepository.findBySettingKey(key);
        if (st == null) {
            st = new Setting();
            st.setKey(key);
            st.setValue(defaultValue);
            settingRepository.save(st);
        }

        return st;
    }

    public Setting get(String key) {

        Setting st = settingRepository.findBySettingKey(key);
        if (st == null) {
            st = new Setting();
            st.setKey(key);
            st.setValue("");
            settingRepository.save(st);
        }

        return st;
    }

    public boolean isExist(String key) {
        return settingRepository.findBySettingKey(key) != null;
    }

    public void put(String key, String value) {

        Setting st = isExist(key)
                ? settingRepository.findBySettingKey(key)
                : new Setting();

        st.setKey(key);
        st.setValue(value);
        settingRepository.save(st);
    }

    public void put(Setting st) {
        put(st.getKey(), st.getValue());
    }

}
