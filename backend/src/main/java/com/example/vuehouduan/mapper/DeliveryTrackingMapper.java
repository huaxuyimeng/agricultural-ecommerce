package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.DeliveryTracking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DeliveryTrackingMapper extends BaseMapper<DeliveryTracking> {

    @Select("SELECT * FROM delivery_tracking WHERE delivery_id = #{deliveryId} ORDER BY create_time DESC")
    List<DeliveryTracking> selectByDeliveryId(@Param("deliveryId") Long deliveryId);
}
