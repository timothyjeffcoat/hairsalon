# YourHairSalon Booking System

A comprehensive appointment booking and management system for hair salons and service-based businesses, built with Spring Framework and modern Java web technologies.

## Overview

YourHairSalon Booking System is a robust web application that provides complete booking management capabilities for service-based businesses. The system includes staff management, service scheduling, client management, payment processing via PayPal, and comprehensive reporting features.

## Technology Stack

- **Java 7** - Core development platform
- **Spring Framework 4.1.6** - Application framework and dependency injection
- **Spring ROO 1.1.5** - Rapid application development
- **Spring Security 3.2.7** - Authentication and authorization
- **Hibernate 4.3.8** - Object-relational mapping
- **PostgreSQL** - Primary database
- **Apache LDAP** - User authentication directory
- **PayPal API** - Payment processing (Classic and REST APIs)
- **Maven 3.x** - Build automation and dependency management
- **jQuery & Dojo Toolkit** - Frontend JavaScript frameworks

## Features

### Core Functionality
- **Appointment Management** - Schedule, modify, and track appointments
- **Staff Management** - Manage staff profiles, schedules, and permissions
- **Service Management** - Define services, pricing, and availability
- **Client Management** - Customer profiles and appointment history
- **Payment Processing** - Integrated PayPal payment handling
- **Reporting** - Comprehensive business reports and analytics

### Security Features
- **Environment Variable Configuration** - All sensitive credentials externalized
- **LDAP Integration** - Centralized user authentication
- **SQL Injection Protection** - Parameterized queries throughout
- **Secure Password Handling** - No password logging or exposure
- **Spring Security Integration** - Role-based access control

### Code Quality
- **CheckStyle** - Code style analysis and enforcement
- **FindBugs** - Static code analysis for bug detection  
- **PMD** - Source code analyzer for code quality
- **Unit Testing** - JUnit-based test coverage

## Quick Start

### Prerequisites
- Java 7 or higher
- Maven 3.x
- PostgreSQL 9.x+
- Apache Directory Server (for LDAP)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd yourhairsalon-booking
   ```

2. **Set up environment variables**
   ```bash
   cp .env.example .env
   # Edit .env with your specific configuration
   ```

3. **Configure database**
   - Create PostgreSQL database named `yourhairsalon`
   - Update database connection settings in `.env`

4. **Configure LDAP**
   - Set up Apache Directory Server
   - Update LDAP settings in `.env`

5. **Configure PayPal**
   - Obtain PayPal API credentials
   - Update PayPal configuration in `.env`

6. **Build and run**
   ```bash
   mvn clean install
   mvn tomcat:run
   ```

7. **Access the application**
   - Open browser to `http://localhost:8080/yourhairsalon-booking`

## Configuration

The application uses environment variables for all configuration to maintain security best practices. See `.env.example` for all available configuration options.

### Key Configuration Areas

- **Database**: PostgreSQL connection settings
- **LDAP**: Authentication directory configuration  
- **Email**: SMTP server settings for notifications
- **PayPal**: Payment processing API credentials
- **SMS**: Nexmo integration for SMS notifications

## Development

### Project Structure
```
src/main/java/com/yourhairsalon/booking/
├── domain/          # Entity classes and domain logic
├── web/             # Spring MVC controllers  
├── task/            # Background processing and helpers
├── form/            # Form binding objects
├── util/            # Utility classes
├── reference/       # Enums and reference data
├── messaging/       # JMS message processing
└── authenticaton/   # Security and authentication
```

### Code Quality Tools

Run code quality checks:
```bash
# CheckStyle analysis
mvn checkstyle:check

# FindBugs analysis  
mvn findbugs:check

# PMD analysis
mvn pmd:check

# All quality checks
mvn validate
```

### Testing
```bash
# Run unit tests
mvn test

# Run integration tests
mvn integration-test
```

## Deployment

### Environment Preparation
1. Set up production database with proper backup strategy
2. Configure production LDAP directory
3. Obtain production PayPal API credentials
4. Set up email server for notifications
5. Configure environment variables for production

### Deployment Steps
```bash
# Build production package
mvn clean package -Pprod

# Deploy WAR file to application server
cp target/yourhairsalon-booking-*.war $TOMCAT_HOME/webapps/
```

## API Integration

### PayPal Integration
The system supports both Classic and REST PayPal APIs for maximum flexibility:
- Classic API for traditional payment processing
- REST API for modern integration patterns

### LDAP Integration
Centralized user authentication with configurable LDAP directory support.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Run code quality checks (`mvn validate`)
4. Commit your changes (`git commit -m 'Add amazing feature'`)
5. Push to the branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

## Security Considerations

- All sensitive configuration is externalized to environment variables
- SQL injection protection through parameterized queries
- Password security with no logging of sensitive credentials
- HTTPS recommended for production deployments
- Regular security updates for all dependencies

## Support

For technical support and questions:
- Review the documentation in the `docs/` directory
- Check the issue tracker for known problems
- Contact the development team

## License

Copyright (c) 2015 YourHairSalon Pro. All rights reserved.

This software is proprietary and confidential. Redistribution and use in source and binary forms, with or without modification, are prohibited without express written permission from YourHairSalon Pro.

---

**YourHairSalon Booking System** - Professional appointment management for service businesses.