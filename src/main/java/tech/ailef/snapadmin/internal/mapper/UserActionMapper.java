package tech.ailef.snapadmin.internal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import tech.ailef.snapadmin.internal.model.UserAction;

@Mapper
public interface UserActionMapper extends BaseMapper<UserAction> {
	
}
