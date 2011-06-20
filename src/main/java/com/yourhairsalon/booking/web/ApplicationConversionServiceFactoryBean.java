package com.yourhairsalon.booking.web;

import com.yourhairsalon.booking.domain.Addresses;
import com.yourhairsalon.booking.domain.Appointment;
import com.yourhairsalon.booking.domain.BaseService;
import com.yourhairsalon.booking.domain.ClientGroup;
import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.Communications;
import com.yourhairsalon.booking.domain.CustomService;
import com.yourhairsalon.booking.domain.Email;
import com.yourhairsalon.booking.domain.Payments;
import com.yourhairsalon.booking.domain.PaymentsType;
import com.yourhairsalon.booking.domain.Reports;
import com.yourhairsalon.booking.domain.ServiceGroup;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.ShopSchedule;
import com.yourhairsalon.booking.domain.ShopSettings;
import com.yourhairsalon.booking.domain.Staff;
import com.yourhairsalon.booking.domain.StaffSchedule;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.roo.addon.web.mvc.controller.RooConversionService;
        
/**
 * A central place to register application Converters and Formatters. 
 */
@RooConversionService
public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {

	@Override
	protected void installFormatters(FormatterRegistry registry) {
		super.installFormatters(registry);
		// Register application converters and formatters
	}
	

	public void installLabelConverters(FormatterRegistry registry) {
        registry.addConverter(new AddressesConverter());
        registry.addConverter(new AppointmentConverter());
        registry.addConverter(new BaseServiceConverter());
        registry.addConverter(new ClientGroupConverter());
        registry.addConverter(new ClientsConverter());
        registry.addConverter(new CommunicationsConverter());
        registry.addConverter(new CustomServiceConverter());
        registry.addConverter(new EmailConverter());
        registry.addConverter(new PaymentsConverter());
        registry.addConverter(new PaymentsTypeConverter());
        registry.addConverter(new ReportsConverter());
        registry.addConverter(new ServiceGroupConverter());
        registry.addConverter(new ShopConverter());
        registry.addConverter(new ShopScheduleConverter());
        registry.addConverter(new ShopSettingsConverter());
        registry.addConverter(new StaffConverter());
        registry.addConverter(new StaffScheduleConverter());
    }

	public void afterPropertiesSet() {
        super.afterPropertiesSet();
        installLabelConverters(getObject());
    }

	static class AddressesConverter implements Converter<Addresses, String> {
        public String convert(Addresses addresses) {
            return new StringBuilder().append(addresses.getAddress1()).append(" ").append(addresses.getAddress2()).append(" ").append(addresses.getCitycode()).append(" ").append(addresses.getStatecode()).toString();
        }
        
    }

	static class AppointmentConverter implements Converter<Appointment, String> {
        public String convert(Appointment appointment) {
            return new StringBuilder().append(appointment.getRecur_parent()).append(" ").append(appointment.getFrequency_week()).append(" ").append(appointment.getDuration_month()).append(" ").append(appointment.getReoccur_start_date()).toString();
        }
        
    }

	static class BaseServiceConverter implements Converter<BaseService, String> {
        public String convert(BaseService baseService) {
            return new StringBuilder().append(baseService.getDescription()).append(" ").append(baseService.getProcesstime()).append(" ").append(baseService.getFinishtime()).append(" ").append(baseService.getMinsetup()).toString();
        }
        
    }

	static class ClientGroupConverter implements Converter<ClientGroup, String> {
        public String convert(ClientGroup clientGroup) {
            return new StringBuilder().append(clientGroup.getGroup_name()).append(" ").append(clientGroup.getGroup_notes()).append(" ").append(clientGroup.getCreateddate()).append(" ").append(clientGroup.getNumber_clients()).toString();
        }
        
    }

	static class ClientsConverter implements Converter<Clients, String> {
        public String convert(Clients clients) {
            return new StringBuilder().append(clients.getFirstName()).append(" ").append(clients.getLastName()).append(" ").append(clients.getBirthDay()).append(" ").append(clients.getUsername()).toString();
        }
        
    }

	static class CommunicationsConverter implements Converter<Communications, String> {
        public String convert(Communications communications) {
            return new StringBuilder().append(communications.getCommunication_value()).toString();
        }
        
    }

	static class CustomServiceConverter implements Converter<CustomService, String> {
        public String convert(CustomService customService) {
            return new StringBuilder().append(customService.getDescription()).append(" ").append(customService.getProcesstime()).append(" ").append(customService.getFinishtime()).append(" ").append(customService.getMinsetup()).toString();
        }
        
    }

	static class EmailConverter implements Converter<Email, String> {
        public String convert(Email email) {
            return new StringBuilder().append(email.getMessage()).append(" ").append(email.getSentDate()).toString();
        }
        
    }

	static class PaymentsConverter implements Converter<Payments, String> {
        public String convert(Payments payments) {
            return new StringBuilder().append(payments.getDatecreated()).append(" ").append(payments.getAmount()).append(" ").append(payments.getTax()).append(" ").append(payments.getNote()).toString();
        }
        
    }

	static class PaymentsTypeConverter implements Converter<PaymentsType, String> {
        public String convert(PaymentsType paymentsType) {
            return new StringBuilder().append(paymentsType.getAmount()).toString();
        }
        
    }

	static class ReportsConverter implements Converter<Reports, String> {
        public String convert(Reports reports) {
            return new StringBuilder().append(reports.getReport_name()).toString();
        }
        
    }

	static class ServiceGroupConverter implements Converter<ServiceGroup, String> {
        public String convert(ServiceGroup serviceGroup) {
            return new StringBuilder().append(serviceGroup.getGroup_name()).append(" ").append(serviceGroup.getGroup_notes()).append(" ").append(serviceGroup.getCreateddate()).append(" ").append(serviceGroup.getNumber_services()).toString();
        }
        
    }

	static class ShopConverter implements Converter<Shop, String> {
        public String convert(Shop shop) {
            return new StringBuilder().append(shop.getExpiration_date()).append(" ").append(shop.getShop_name()).append(" ").append(shop.getShopuuid()).append(" ").append(shop.getShop_url()).toString();
        }
        
    }

	static class ShopScheduleConverter implements Converter<ShopSchedule, String> {
        public String convert(ShopSchedule shopSchedule) {
            return new StringBuilder().append(shopSchedule.getSchedule_date()).append(" ").append(shopSchedule.getStart_time()).append(" ").append(shopSchedule.getEnd_time()).toString();
        }
        
    }

	static class ShopSettingsConverter implements Converter<ShopSettings, String> {
        public String convert(ShopSettings shopSettings) {
            return new StringBuilder().append(shopSettings.getEmail_subject()).append(" ").append(shopSettings.getEmail_message()).append(" ").append(shopSettings.getEmail_signature()).append(" ").append(shopSettings.getUsername()).toString();
        }
        
    }

	static class StaffConverter implements Converter<Staff, String> {
        public String convert(Staff staff) {
            return new StringBuilder().append(staff.getFirstName()).append(" ").append(staff.getLastName()).append(" ").append(staff.getBirthDay()).append(" ").append(staff.getUsername()).toString();
        }
        
    }

	static class StaffScheduleConverter implements Converter<StaffSchedule, String> {
        public String convert(StaffSchedule staffSchedule) {
            return new StringBuilder().append(staffSchedule.getSchedule_date()).append(" ").append(staffSchedule.getStart_time()).append(" ").append(staffSchedule.getEnd_time()).toString();
        }
        
    }
}
