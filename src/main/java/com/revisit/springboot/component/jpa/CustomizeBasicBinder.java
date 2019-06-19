// /**
//  * Copyright (C), 2015-2019, 美果科技有限公司
//  * FileName: CustomizeBasicBinder
//  * Author:   Revisit-Moon
//  * Date:     2019/2/7 1:35 AM
//  * Description: CustomizeBasicBinder
//  * History:
//  * <author>          <time>          <version>          <desc>
//  * Revisit       2019/2/7 1:35 AM        1.0              描述
//  */
//
// package com.revisit.springboot.component.jpa;
//
// import org.hibernate.internal.CoreLogging;
// import org.hibernate.type.descriptor.JdbcTypeNameMapper;
// import org.hibernate.type.descriptor.ValueBinder;
// import org.hibernate.type.descriptor.WrapperOptions;
// import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
// import org.hibernate.type.descriptor.sql.BasicBinder;
// import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;
// import org.jboss.logging.Logger;
// import org.springframework.stereotype.Component;
//
// import java.sql.CallableStatement;
// import java.sql.PreparedStatement;
// import java.sql.SQLException;
//
// /**
//  * 〈CustomizeBasicBinder〉
//  *
//  * @author Revisit-Moon
//  * @create 2019/2/7
//  * @since 1.0.0
//  */
// @Component
// public class CustomizeBasicBinder<J>implements ValueBinder<J> {
//
//     private static final Logger log = CoreLogging.logger( BasicBinder.class );
//
//     private static final String BIND_MSG_TEMPLATE = "binding parameter [%s] as [%s] - [%s]";
//     private static final String NULL_BIND_MSG_TEMPLATE = "binding parameter [%s] as [%s] - [null]";
//
//     private String sql;
//
//
//     private final JavaTypeDescriptor<J> javaDescriptor;
//     private final SqlTypeDescriptor sqlDescriptor;
//
//     public CustomizeBasicBinder() {
//         sqlDescriptor = null;
//         javaDescriptor = null;
//     }
//
//
//     public JavaTypeDescriptor<J> getJavaDescriptor() {
//         return javaDescriptor;
//     }
//
//     public SqlTypeDescriptor getSqlDescriptor() {
//         return sqlDescriptor;
//     }
//
//     @Override
//     public void bind(PreparedStatement st, J value, int index, WrapperOptions options) throws SQLException {
//         final boolean traceEnabled = log.isTraceEnabled();
//
//         if ( value == null ) {
//             if ( traceEnabled ) {
//                 log.trace(
//                         String.format(
//                                 NULL_BIND_MSG_TEMPLATE,
//                                 index,
//                                 JdbcTypeNameMapper.getTypeName( getSqlDescriptor().getSqlType() )
//                         )
//                 );
//             }
//             st.setNull( index, sqlDescriptor.getSqlType() );
//         }
//         else {
//             if ( traceEnabled ) {
//                 log.trace(
//                         String.format(
//                                 BIND_MSG_TEMPLATE,
//                                 index,
//                                 JdbcTypeNameMapper.getTypeName( sqlDescriptor.getSqlType() ),
//                                 getJavaDescriptor().extractLoggableRepresentation( value )
//                         )
//                 );
//             }
//
//             doBind( st, value, index, options );
//         }
//
//         this.sql = st.toString();
//     }
//
//     @Override
//     public void bind(CallableStatement st, J value, String name, WrapperOptions options) throws SQLException {
//
//     }
//
//     protected void doBind(PreparedStatement st, J value, int index, WrapperOptions options) throws SQLException {
//
//     }
// }
