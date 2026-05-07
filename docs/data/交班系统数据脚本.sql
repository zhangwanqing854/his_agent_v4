--科室信息
select code||chr(9) as code,name,id_dep from bd_dep b where b.sd_depttp in('01','02') and fg_ip='Y' and id_org='0001HY1000000000OH85';

--科室人员信息
select                           
a.code||chr(9) code_user,                                       
a.name name_user,                                       
c.name name_dept,                                       
c.code||chr(9) code_dept                                       
from SYS_USER a                                         
left join SYS_SUBJECT_ORG b on a.id_user=b.subjectid    
left join bd_dep c on b.id_org=c.id_dep                 
where a.id_org ='0001HY1000000000OH85';

--人员信息
 select                                                                                                           
 u.id_user,                                                                                                       
 u.name name_user,                                                                                                
 u.code||chr(9) code_user,                                                                                                
 b.id_psndoc,                                                                                                     
 b.name name_psn,                                                                                                 
 b.code||chr(9) code_psn,                                                                                                 
 b.sd_emptitle,                                                                                                   
 t.name name_emptitle,                                                                                            
 case when b.penelauth = '离职人员' or b.humantype = '0001Z8100000000OVN2B' then '1' else '0' end sd_humantype,     
 case when b.penelauth = '离职人员' or b.humantype = '0001Z8100000000OVN2B' then '离职' else '在职' end name_humantype 
 from sys_user u                                                                                                  
 inner join bd_psndoc b                                                                                           
 on u.id_psn = b.id_psndoc                                                                                        
 left join bd_udidoc t                                                                                            
 on t.id_udidoc = b.id_emptitle                                                                                   
 where b.ds='0'                                                                                            
 and b.id_org= '0001HY1000000000OH85';     
 
 
 --就诊信息
 select 									
e.id_ent,                                  
ent.code_entp||chr(9) as code_entp,                             
ent.code||chr(9) as code_ent,                         
ent.dt_acpt,                               
ent.dt_end,                                
ent.fg_ip,                                 
e.times_ip,                                
ent.id_pat,                                
ent.code_entp||chr(9) as code_entp,                             
pat.name name_pat,                         
pat.code||chr(9) as code_pat,                         
ent.id_dep_phy,                            
dp.code||chr(9) code_dep_phy,                      
dp.name name_dep_phy,                      
ent.id_dep_nur,                            
dr.code||chr(9) code_dep_nur,                      
dr.name name_dep_nur,                      
e.sd_level_nur||chr(9) as sd_level_nur,                            
ln.name name_level_nur,                    
e.name_bed                                 
from en_ent_ip e                           
inner join en_ent ent                      
on ent.id_ent = e.id_ent                   
inner join pi_pat pat                      
on pat.id_pat = ent.id_pat                 
left join bd_dep dp                        
on dp.id_dep = ent.id_dep_phy              
left join bd_dep dr                        
on dr.id_dep = ent.id_dep_nur              
left join bd_udidoc ln                     
on ln.id_udidoc = e.id_level_nur           
where ent.id_org= '0001HY1000000000OH85' and ent.fg_ip='Y' and ent.code_entp in ('10','0103') and ent.dt_acpt is not null;


--患者信息

select 										
pat.name name_pat,                           
pat.code||chr(9) as code,                                    
sd_sex,                                      
u1.name as name_sex,                         
pat.dt_birth,                                
pat.dt_birth_hms,                            
pat.tel,                                     
pat.mob,                                     
pat.sd_nation||chr(9) as sd_nation,                               
u2.name name_nation,                         
pat.sd_country,                              
 u3.name name_country,                       
pat.sv                                       
from pi_pat pat    
inner join en_ent ent 
on pat.id_pat=ent.id_pat   
inner join en_ent_ip eip
on eip.id_ent = ent.id_ent                       
left join bd_udidoc u1                       
on pat.id_sex = u1.id_udidoc                 
left join bd_udidoc u2                       
on pat.id_nation = u2.id_udidoc              
left join BD_COUNTRY u3                      
on pat.id_country = u3.id_countryzone      
where ent.fg_ip='Y';                                                                                   
