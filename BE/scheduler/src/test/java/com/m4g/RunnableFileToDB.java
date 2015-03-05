package com.m4g;
/*
import com.coral.project.dao.IEntityPopulationDao;
import com.coral.project.entities.IEntityPopulation;
import com.coral.project.entities.IEntityPopulationId;
import com.m4g.ErrorSeverity;
import com.m4g.task.TaskContext;
import com.m4g.task.TaskContextAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;

@Transactional
@Component("runnableFileToDB")
public class RunnableFileToDB implements Runnable, Serializable, TaskContextAware{

	private static final long serialVersionUID = -8323122287604538449L;
	private TaskContext taskContext;
	private IEntityPopulationDao entityPopulationDao;
	
	public IEntityPopulationDao getEntityPopulationDao() {
		return entityPopulationDao;
	}
	
	public RunnableFileToDB(){
	}
	
	private void init(){
		entityPopulationDao = (IEntityPopulationDao) taskContext.getBean("iEntityPopulationDao");
	}

	@Autowired
	public void setEntityPopulationDao(IEntityPopulationDao entityPopulationDao) {
		if (this.entityPopulationDao == null){
			this.entityPopulationDao = entityPopulationDao;
		}
	}

	@Override
	public void setTaskContext(TaskContext taskContext) {
		this.taskContext = taskContext;
		init();
	}

	@Override
	public boolean hasTaskContext() {
		return taskContext != null;
	}
	
	private IEntityPopulation proccessLine(String csvLine){
		String [] valuesArray = csvLine.split(",");
		IEntityPopulation entity = new IEntityPopulation(); 
		IEntityPopulationId entityId = new IEntityPopulationId();
		entityId.setIdentityNo(valuesArray[0]);
		entityId.setRecId(Short.parseShort(valuesArray[1]));
		entity.setIdentityNo(entityId);
		entity.setFamilyName(valuesArray[2]);
		entity.setPrivateName(valuesArray[3]);
		entity.setBirthDate(null);		
		entity.setFatherName(valuesArray[5]);
		entity.setMotherName(valuesArray[6]);
		entity.setPreviousFamilyName(valuesArray[7]);
		entity.setNumberOfChildern(Integer.parseInt(valuesArray[8]));
		entity.setChildTill18(Integer.parseInt(valuesArray[9]));
		entity.setCityNum(Integer.parseInt(valuesArray[10]));
		entity.setCityName(valuesArray[11]);
		entity.setStreetNum(Integer.parseInt(valuesArray[12]));
		entity.setStreetName(valuesArray[13]);
		

		
		//entity.setHouseNum(houseNum);
		//entity.setApartNum(apartNum);
		//entity.setZipCode(zipCode);
		//entity.setGender(genderCd);
		//entity.setFamilyStatusCd(familyStatusCd);
		//entity.setInsertDate(insertDate);
		//entity.setInsertUser(insertUser);
		//entity.setUpdateDate(updateDate);
		//entity.setUpdateUser(updateUser);
		//entity.setPageUrl(pageUrl);
		//entity.setEntityStatusCd(entityStatusCd);
		//entity.setEntityStatusDate(entityStatusDate);
		//entity.setNationCd(nationCd)
		

		
		return entity;
	}
	
	private boolean existEntityInDB(IEntityPopulation entity){
		IEntityPopulationId idEntity =  entity.getIdentityNo();
		String idNum = idEntity.getIdentityNo();
		short recId = idEntity.getRecId();
		IEntityPopulation dbEntity  = entityPopulationDao.findSimple(idNum, recId);
		return dbEntity != null;
	}
	
	// This is a short equal comparing only few column NOT the whole check
	//Instead need to use equals
	private boolean isEqual(IEntityPopulation entity){
		IEntityPopulationId idEntity =  entity.getIdentityNo();
		String idNum = idEntity.getIdentityNo();
		short recId = idEntity.getRecId();
		IEntityPopulation dbEntity  = entityPopulationDao.findSimple(idNum, recId);
		if (dbEntity.getIdentityNo().equals(entity.getIdentityNo())){
//			if (dbEntity.getFamilyName().equals(entity.getFamilyName())){
//				if (dbEntity.getPrivateName().equals(entity.getPrivateName())){
//					return true;
//				}
//			}
			return true;
		}
		return false;
	}
	
	private void persistEntity(IEntityPopulation entity){
		if (entity == null){
			return;
		}
		if (existEntityInDB(entity)){
			if (!isEqual(entity)){
				entityPopulationDao.save(entity);
			}
		}else{
			entityPopulationDao.save(entity);
		}
	}
	

	@Override
	public void run() {
		try{
			//FileInputStream fstream = new FileInputStream("d:\\textfile.txt");
			FileInputStream fileInputStream = new FileInputStream("d:\\entityPopulation.txt");
			DataInputStream dataInputStream = new DataInputStream(fileInputStream);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
			String csvLine;
			while ((csvLine = bufferedReader.readLine()) != null){
				System.out.println (csvLine);
				IEntityPopulation entity = proccessLine(csvLine);
				System.out.println ("---" + entity.toString());
				persistEntity(entity);
			}
			dataInputStream.close();
			taskContext.setProgress(100);
			taskContext.setMessage("Complete proccessing file");
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
			taskContext.setMessage("problem while proccesing file ");
			taskContext.setError("problem while proccesing file ",e.getMessage(), ErrorSeverity.ERROR);
		}
	}		


}
*/