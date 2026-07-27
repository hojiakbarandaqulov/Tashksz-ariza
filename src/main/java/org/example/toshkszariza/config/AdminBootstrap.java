package org.example.toshkszariza.config;

import org.example.toshkszariza.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AdminService adminService;
    private final BotProperties properties;

    public AdminBootstrap(AdminService adminService, BotProperties properties) {
        this.adminService = adminService;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.adminId() > 0) {
            adminService.registerConfiguredAdmin(properties.adminId());
            log.info("Konfiguratsiyadagi Telegram ID administrator sifatida saqlandi");
        } else if (properties.autoCreateAdmin()) {
            log.info("Admin hali belgilanmagan bo'lsa, birinchi /start yuborgan foydalanuvchi bosh admin bo'ladi");
        }
    }
}
