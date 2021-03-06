package com.xtesoft.mined.services;

import com.xtesoft.mined.clients.KeycloakClient;
import com.xtesoft.mined.clients.SGIPDClient;
import com.xtesoft.mined.clients.pojos.keycloak.KeycloakTokenPojo;
import com.xtesoft.mined.clients.pojos.sgipd.Criterio5Result;
import com.xtesoft.mined.clients.pojos.sgipd.ReporteDocentesAplicaciones;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import java.util.List;

@ApplicationScoped
public class SGIPDService {
    public static final int BEGIN = 126000;
    public static final int END = 320966;
    public static final int BY= 10000;
    private static final Logger LOG = Logger.getLogger(SGIPDService.class);
    public static final String BEARER="Bearer ";
    @Inject
    @RestClient
    SGIPDClient sgipdClient;

    @Inject
    KeycloakService keycloakService;

    public List<ReporteDocentesAplicaciones> getAplicacionesDocente(Integer codigoSolicitante){
        KeycloakTokenPojo token = keycloakService.getToken();
        return sgipdClient.getAplicacionesDocente(BEARER+token.access_token,codigoSolicitante);
    }

    public Criterio5Result saveCriterio5ResidenciaDocente(Integer pageSize,Integer from){
        KeycloakTokenPojo token = keycloakService.getToken();
        return sgipdClient.saveCriterio5ResidenciaDocente(BEARER+token.access_token,pageSize,from);
    }

    public Long getCountAplicaciones(){
        KeycloakTokenPojo token = keycloakService.getToken();
        return sgipdClient.getCountAplicaciones(BEARER+token.access_token);
    }

    //
    @Scheduled(cron = "5 20 23 7 DEC ? 2021")
    void runCriterio5Restante(ScheduledExecution execution){
        LOG.info(execution.getScheduledFireTime());
        for (int i = BEGIN; i < END ; i=i+BY) {
            Criterio5Result r = saveCriterio5ResidenciaDocente(BY,BEGIN);
            LOG.info("Current: "+i);
            LOG.info(r);
        }

    }
    @Scheduled(cron = "0 35 10 8 DEC ? 2021")
    public void runCriterio5Full(ScheduledExecution execution){
        Long count = this.getCountAplicaciones();
        LOG.info("Execution time: "+execution.getScheduledFireTime());
        for (int i = 0; i <= count.intValue(); i=i+BY) {
            LOG.info("Processing: i="+i);
            Criterio5Result r = saveCriterio5ResidenciaDocente(BY,i);
            LOG.info("Result: "+r);
        }
    }




}
