package ${packageName}.entity.${entityName?lower_case};

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 实体基础类
 *
 */
@MappedSuperclass
@Data
public class BasicEntity implements Serializable {
    @Id
    @Column(name = "id", unique = true, nullable = false, length = 32)
    @GenericGenerator(name = "system-uuid", strategy = "${packageName}.component.uuid.CustomizeUUIDGenerate" )
    @GeneratedValue(generator = "system-uuid")
    private String id;                          //主键

    @Excel(name = "创建时间", exportFormat = "yyyy-MM-dd HH:mm:ss",orderNum = "1")
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @Column(name="create_time", nullable=false, length=19)
    private Date createTime;                    //创建时间

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @Column(name="update_time", nullable=false, length=19)
    private Date updateTime;                    //更新时间

    public BasicEntity() {
        Date now = new Date();
        this.createTime = now;
        this.updateTime = now;
    }
}