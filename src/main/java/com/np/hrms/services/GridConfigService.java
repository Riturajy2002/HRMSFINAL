package com.np.hrms.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.np.hrms.entities.GridConfig;
import com.np.hrms.model.Action;
import com.np.hrms.model.GridDTO;
import com.np.hrms.model.Param;
import com.np.hrms.repositories.RefMasterRepository;
import com.np.hrms.repositories.SqlDAO;

@Service
public class GridConfigService {

    @Autowired 
    private SqlDAO sqlDao;

    @Autowired 
    private RefMasterRepository refMasterRepository;

    public GridDTO loadScreenConfig(String screenname) {

        Gson gsonObj = new Gson();
        GridConfig gdConfig = sqlDao.loadScreen(screenname).get(0);
        String paramBody = gdConfig.getParams();
        String actionBody = gdConfig.getActions();
        
        GridDTO gdDto = gsonObj.fromJson(paramBody, GridDTO.class);
      
        
        gdDto.setScreenName(gdConfig.getScreenName());
        gdDto.setPageTitle(gdConfig.getPageTitle());
        gdDto.setScreenDesc(gdConfig.getDesc());
        gdDto.setScreenQuery(gdConfig.getDataQuery());

        for (String paramType : gdDto.getParams().keySet()) {
            Map<String, Param> paramMap = gdDto.getParams().get(paramType);

            for (Map.Entry<String, Param> entry : paramMap.entrySet()) {
                Param param = entry.getValue();
                
                if ("ref_master".equals(param.getSourceType())) {
                    String sourceId = param.getSourceId();
                    gdDto.getRefData().put(sourceId, refMasterRepository.fetchRefData(sourceId, null));
                }
            }
        }
        
		
		  Map<String, Action> actionMap = gsonObj.fromJson(actionBody, Map.class);
		  gdDto.setActions(actionMap);
		 

        return gdDto;
    }
}
