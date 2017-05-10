@echo off

mvn clean package && md target\service-m && md target\service-m\bin && md target\service-m\pkg && md target\service-m\lib && md target\service-m\log && xcopy bin target\service-m\bin && xcopy pkg target\service-m\pkg && xcopy target\service-m.jar target\service-m\lib
