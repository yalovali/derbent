/*********************************************************
  * Project Name    : ECEMTAG DIAGNOSTIC TOOL
  * Project Code    : ST
  * Author          : yalovali@gmail.com
  * Creation Date   : 12.05.2023
  * Description     : Diagnostic tool application for ECU/TCU
  **********************************************************
  * Purpose         : Header file for CA2LParser of ST project
  **********************************************************/
#include "defines.h"
/* disable compiler warnings on external includes of QT. Internal includes also added to reduce multiple warnings */
DISABLE_ALL_WARNINGS_BEGIN
#include <cmath>
#include <QFile>
#include <QJsonArray>

#include <cenums.h>

#include "ca2lparser.h"
#include "cauxillary.h"
#include "cauxillarygui.h"
#include "cexception.h"
DISABLE_ALL_WARNINGS_END
/*******************************************/
constexpr auto S_BEGIN_CHARACTERISTIC = "/begin CHARACTERISTIC";
constexpr auto S_BEGIN_AXIS_DESCR = "/begin AXIS_DESCR";
constexpr auto S_BEGIN_AXIS_PTS = "/begin AXIS_PTS";
constexpr auto S_END_CHARACTERISTIC = "/end CHARACTERISTIC";
constexpr auto S_BEGIN_MEASUREMENT = "/begin MEASUREMENT";
constexpr auto S_END_MEASUREMENT = "/end MEASUREMENT";
constexpr auto S_BEGIN_COMPU_METHOD = "/begin COMPU_METHOD";
constexpr auto S_BEGIN_MEMORY_REGION = "/begin MEMORY_REGION";
/*******************************************/
const QRegularExpression CA2LParser::AddressMatch = QRegularExpression(VALID_ADDRESS_MATCH);
const QRegularExpression CA2LParser::HexMatch = QRegularExpression(VALID_HEX_MATCH);
/*******************************************/
/**
  * @brief CA2LParser::getToken_Commented
  * @param line
  * @param json
  * @param comment
  * @param key
  * @return
  */
bool CA2LParser::getToken_Commented(cstr line,QJsonObject &json,cstr comment,cstr key)
{
    QString token = "/* "+comment+" */";
    if (!line.contains(token,Qt::CaseInsensitive))
    {
        return false;
    }
    QString value = line.section(token,1);
    json.insert(key.isEmpty()?comment:key,
            value);
    return true;
}
/**
  * @brief CA2LParser::parseA2LFile Parse an A2L file into Json format.
  * read from file already parsed A2l
  * if you cannot read it, parse if from raw A2l file and save a copy in json format
  * @param fileName File to parse
  * @return True on success
  */
void CA2LParser::parse_character(QJsonObject &json,QString &state,unsigned long &lineCount,QTextStream &in,
        const QMap <QString,QString> &typeDB,QString &original,QString &line,
        uint &indexCounter)
{
    bool ok = false;
    json = QJsonObject();
    json.insert(J_A2L_SOURCE,CEnums::A2L);
    json.insert(J_A2L_FIELDTYPE,FIELD_TYPE_CHAR);
    state = S_BEGIN_CHARACTERISTIC;
    QString value = line.section(S_BEGIN_CHARACTERISTIC,1).simplified().trimmed().simplified().trimmed();
    if (value.isEmpty())
    {
        //if there is no name in start line, it is here
        value = readline(lineCount,in,original);
    }
    json.insert(J_A2L_NAME,value);
    line = readline(lineCount,in,original);
    json.insert(J_A2L_IDENTIFIER,line);
    line = readline(lineCount,in,original);
    json.insert(J_A2L_DATATYPE,line);	//in chars, this is always value !
    line = readline(lineCount,in,original);
    EXPIF_F(AddressMatch.match(line).hasMatch(),CException_CAN_A2LError,
            QObject::tr("Parse error in a2l file, line:[%1][%2] address:[%3]").arg(
                    lineCount).arg(original).arg(line))
    json.insert(J_A2L_ADDRESS,line);
    json.insert(J_A2L_INDEX,QJsonValue(static_cast <int>(indexCounter)));
    indexCounter++;
    static QRegularExpression reg1("/\\*");
    static QRegularExpression reg2("\\*/");
    json.insert("Address_Original",
            original.replace("/* Address */","").replace(" "+line+" ","").replace(reg1,"").replace(reg2,"").trimmed());
    line = readline(lineCount,in,original);
    auto val = typeDB.value(line,line);
    EXPIF(val.isEmpty(),CException_CAN_A2LError,QString(
            "Unsupported type in A2L for key:[%1] record_type:[%2]").arg(
                    line,val))
    json.insert(J_A2L_RECORDTYPE,val);
    line = readline(lineCount,in,original);
    json.insert(J_AXIS0_MAXDIFF,line);
    line = readline(lineCount,in,original);
    json.insert(J_A2L_CONVERSION,line);
#if defined DEBUG_A2L
    if (line=="EcuIntgdSwGend_CM_single_rpm")
    {
        DEBUG << "aaa" << line << lineCount;
    }
#endif
    line = readline(lineCount,in,original);
    json.insert(J_A2L_LOWER_LIMIT,line.toDouble(&ok));
    EXPIF_F(ok,CException_CAN_A2LError,QObject::tr("Parse error in a2l file, line:[%1][%2]").arg(
            lineCount).arg(original))
    line = readline(lineCount,in,original);
    json.insert(J_A2L_UPPER_LIMIT,line.toDouble(&ok));
    EXPIF_F(ok,CException_CAN_A2LError,QObject::tr("Parse error in a2l file, line:[%1][%2]").arg(
            lineCount).arg(original))
}	//CA2LParser::parse_character
/**
  * @brief CA2LParser::parse_measurement Parse measurement section of A2L file
  * @param json
  * @param state
  * @param lineCount
  * @param in
  * @param typeDB
  * @param original
  * @param line
  */
void CA2LParser::parse_measurement(QJsonObject &json,
        QString &state,
        unsigned long &lineCount,
        QTextStream &in,
        const QMap <QString,QString> &typeDB,
        QString &original,
        QString &line)
{
    bool ok = false;
    json = QJsonObject();
    json.insert(J_A2L_SOURCE,CEnums::A2L);
    json.insert(J_A2L_FIELDTYPE,FIELD_TYPE_MEASURE);
    QString value = line.section(S_BEGIN_MEASUREMENT,1).simplified().trimmed();
    if (value.isEmpty())
    {
        //if there is no name in start line, it is here
        value = readline(lineCount,in,original);
    }
    json.insert(J_A2L_NAME,value);
    line = readline(lineCount,in,original);
    json.insert(J_A2L_IDENTIFIER,line);
    line = readline(lineCount,in,original);
    QString val = typeDB.value(line,"");
    if (val.isEmpty())
    {
        EXPIF(true,CException_CAN_A2LError,QString(
                "Unsupported type in A2L for key:[%1] record_type:[%2]").arg(
                        line,val))
    }
    json.insert(J_A2L_RECORDTYPE,val);	//swap this line from chars.
    line = readline(lineCount,in,original);
    json.insert(J_A2L_CONVERSION,line);
#if defined DEBUG_A2L
    //if (line=="EcuIntgdSwGend_CM_single_rpm")
    //{
    //DEBUG << "aaa" << line << lineCount;
    //}
#endif
    line = readline(lineCount,in,original);
    json.insert(J_A2L_RESOLUTION,line);
    line = readline(lineCount,in,original);
    json.insert(J_A2L_ACCURACY,line);
    line = readline(lineCount,in,original);
    json.insert(J_A2L_LOWER_LIMIT,line.toDouble(&ok));
    EXPIF_F(ok,CException_CAN_A2LError,QString("Parse error in a2l file line %1 %2").arg(
            lineCount).arg(original))
    line = readline(lineCount,in,original);
    json.insert(J_A2L_UPPER_LIMIT,line.toDouble(&ok));
    EXPIF_F(ok,CException_CAN_A2LError,QString("Parse error in a2l file line %1 %2").arg(
            lineCount).arg(original))
    state = S_BEGIN_MEASUREMENT;
}	//CA2LParser::parse_measurement
/**
  * @brief CA2LParser::parse_compute
  * @param json
  * @param state
  * @param lineCount
  * @param in
  * @param original
  * @param line
  * @param jsonA2L
  */
void CA2LParser::parse_compute(QJsonObject &json,
        QString &state,
        unsigned long &lineCount,
        QTextStream &in,
        QString &original,
        QString &line,
        QJsonObject &jsonA2L)
{
    json = QJsonObject();
    json.insert(J_A2L_SOURCE,CEnums::A2L);
    json.insert(J_A2L_FIELDTYPE,FIELD_TYPE_COMPU_METHOD);
    state = S_BEGIN_COMPU_METHOD;
    QString value = line.section(S_BEGIN_COMPU_METHOD,1).simplified().trimmed();
    if (value.isEmpty())
    {
        //if there is no name in start line, it is here
        value = readline(lineCount,in,original);
    }
    json.insert(J_A2L_NAME,value);
    line = readline(lineCount,in,original);
    json.insert(J_A2L_IDENTIFIER,line);
    line = readline(lineCount,in,original);
    json.insert(J_A2L_DATATYPE,line);
    line = readline(lineCount,in,original);
    json.insert(J_A2L_FORMAT,line);
    line = readline(lineCount,in,original);
    json.insert(J_A2L_UNITS,line);
    line = readline(lineCount,in,original);
    json.insert(J_A2L_COEFFICIENTS,line);
    auto type_set = jsonA2L.value(J_A2L_COMPUTEMETHODS).toObject(QJsonObject());
    type_set.insert(json.value(J_A2L_NAME).toString(""),json);
    jsonA2L.insert(J_A2L_COMPUTEMETHODS,type_set);
    line = readline(lineCount,in,original);	//end of compu_method //skip this
    state = "none";
}	//CA2LParser::parse_compute
/**
  * @brief CA2LParser::parse_memory
  * @param json
  * @param state
  * @param lineCount
  * @param in
  * @param original
  * @param line
  * @param jsonA2L
  * @param skip
  */
void CA2LParser::parse_memory(QJsonObject &json,
        QString &state,
        unsigned long &lineCount,
        QTextStream &in,
        QString &original,
        QString &line,
        QJsonObject &jsonA2L,
        bool &skip)
{
    skip = false;
    json = QJsonObject();
    json.insert(J_A2L_SOURCE,CEnums::A2L);
    json.insert(J_A2L_FIELDTYPE,FIELD_TYPE_MEMORY_REGION);
    state = S_BEGIN_MEMORY_REGION;
    QString value = readline(lineCount,in,original);
    QStringList sections = value.split(" ");
    if (sections.count()!=5)
    {
        ERRLOG << "Line:" << lineCount << " " << QString(
                "Error in memory region section:%1").arg(value);
        skip = true;
        return;
    }
    QString name = sections.at(0);
    QString virtualAddress = sections.at(1);
    QString physicalAddress = sections.at(2);
    QString size = sections.at(3);
    bool ok = false;
    [[maybe_unused]] ulong u_virtualAddress = virtualAddress.toULong(&ok,BASE16);	//just check it
    //Q_UNUSED(u_virtualAddress)
    if (!ok)
    {
        ERRLOG << "Line:" << lineCount << " " << QString(
                "Error in memory region section:%1").arg(virtualAddress);
        EXPIF_F(ok,CException_CAN_A2LError,QString("Parse error in a2l file line %1 %2").arg(
                lineCount).arg(original))
    }
    [[maybe_unused]] ulong u_physicalAddress = physicalAddress.toULong(&ok,BASE16);
    if (!ok)
    {
        ERRLOG << "Line:" << lineCount << " " << QString(
                "Error in memory region section:%1").arg(physicalAddress);
        EXPIF_F(ok,CException_CAN_A2LError,QString("Parse error in a2l file line %1 %2").arg(
                lineCount).arg(original))
    }
    [[maybe_unused]] uint u_size = size.toULong(&ok,BASE16);
    if (!ok)
    {
        ERRLOG << "Line:" << lineCount << " " << QString(
                "Error in memory region section:%1").arg(size);
        EXPIF_F(ok,CException_CAN_A2LError,QString("Parse error in a2l file line %1 %2").arg(
                lineCount).arg(original))
    }
    json.insert(J_A2L_NAME,name);
    EXPIF_F(HexMatch.match(virtualAddress).hasMatch(),CException_CAN_A2LError,
            QString("Parse error in a2l file line %1 %2 address [%3]").arg(
                    lineCount).arg(original).arg(virtualAddress))
    json.insert(J_A2L_ADDRESS_START,virtualAddress);
    EXPIF_F(HexMatch.match(physicalAddress).hasMatch(),CException_CAN_A2LError,
            QString("Parse error in a2l file line %1 %2 address [%3]").arg(
                    lineCount).arg(original).arg(physicalAddress))
    json.insert(J_A2L_ADDRESS_LENGTH,physicalAddress);
    json.insert(J_A2L_FORMAT,size);
    QJsonObject type_set = jsonA2L.value(J_A2L_MEMORYREGIONS).toObject(QJsonObject());
    type_set.insert(json.value(J_A2L_NAME).toString(""),json);
    jsonA2L.insert(J_A2L_MEMORYREGIONS,type_set);
    line = readline(lineCount,
            in,
            original);	//end of compu_method //skip this
    state = "none";
}	//CA2LParser::parse_memory
/**
  * @brief CA2LParser::parse_axis
  * @param json
  * @param state
  * @param lineCount
  * @param in
  * @param typeDB
  * @param original
  * @param line
  */
void CA2LParser::parse_axis(QJsonObject &json,QString &state,unsigned long &lineCount,QTextStream &in,
        const QMap <QString,QString> &typeDB,QString &original,QString &line)
{
    bool ok = false;
    json = QJsonObject();
    json.insert(J_A2L_SOURCE,CEnums::A2L);
    json.insert(J_A2L_FIELDTYPE,FIELD_TYPE_AXIS_PTS);
    state = S_BEGIN_AXIS_PTS;
    QString value = line.section(S_BEGIN_AXIS_PTS,1).simplified().trimmed().simplified().trimmed();
    if (value.isEmpty())
    {
        //if there is no name in start line, it is here
        value = readline(lineCount,in,original);
    }
    json.insert(J_A2L_NAME,value);
    line = readline(lineCount,in,original);
    json.insert(J_A2L_IDENTIFIER,line);
    line = readline(lineCount,in,original);
    EXPIF_F(AddressMatch.match(line).hasMatch(),CException_CAN_A2LError,
            QString("Parse error in a2l file line %1 %2 address [%3]").arg(
                    lineCount).arg(original).arg(line))
    json.insert(J_A2L_ADDRESS,line);
    //json.insert(J_A2L_INDEX,QString("%1").arg(indexCounter));
    //indexCounter++;
    static QRegularExpression reg3("\\*/");
    static QRegularExpression reg4("/\\*");
    json.insert("Address_Original",
            original.replace("/* Address */","").replace(" "+line+" ","").replace(reg4,"").replace(reg3,"").trimmed());
    line = readline(lineCount,in,original);
    json.insert(J_AXIS0_INPUTQUANTITY,line);
    line = readline(lineCount,in,original);
    QString val = typeDB.value(line,"");
    CHECKIF(val.isEmpty(),QString("Unsupported type in A2L for key:[%1] record_type:[%2]").arg(
            line,val))
    json.insert(J_A2L_RECORDTYPE,val);	//swap this line from chars.
    line = readline(lineCount,in,original);
    json.insert(J_AXIS0_MAXDIFF,line.toDouble(&ok));
    EXPIF_F(ok,CException_CAN_A2LError,QString("Parse error in a2l file line %1 %2").arg(
            lineCount).arg(original))
    line = readline(lineCount,in,original);
    json.insert(J_AXIS0_CONVERSION,line);
    line = readline(lineCount,in,original);
    json.insert(J_AXIS0_NUMBER_OF_POINTS,line.toInt(&ok));
    EXPIF_F(ok,CException_CAN_A2LError,QString("Parse error in a2l file line %1 %2").arg(
            lineCount).arg(original))
    line = readline(lineCount,in,original);
    json.insert(J_AXIS0_LOWER_LIMIT,line.toDouble(&ok));
    EXPIF_F(ok,CException_CAN_A2LError,QString("Parse error in a2l file line %1 %2").arg(
            lineCount).arg(original))
    line = readline(lineCount,in,original);
    json.insert(J_AXIS0_UPPER_LIMIT,line.toDouble(&ok));
    EXPIF_F(ok,CException_CAN_A2LError,QString("Parse error in a2l file line %1 %2").arg(
            lineCount).arg(original))
}	//CA2LParser::parse_axis
/**
  * @brief CA2LParser::initialize_typeDB Fills in the typeDB used in variable type conversion of A2L entry data types
  * @param typeDB Map of type conversions
  */
void CA2LParser::initialize_typeDB(QMap <QString,QString> &typeDB)
{
    typeDB.insert("Lookup1D_BOOLEAN",T_UBYTE);
    typeDB.insert("Lookup1D_BYTE",T_UBYTE);
    typeDB.insert("Lookup1D_FLOAT32_IEEE",T_FLOAT32_IEEE);
    typeDB.insert("Lookup1D_SLONG",T_SLONG);
    typeDB.insert("Lookup1D_SLONG",T_ULONG);
    typeDB.insert("Lookup1D_UBYTE",T_UBYTE);
    typeDB.insert("Lookup1D_ULONG",T_ULONG);
    typeDB.insert("Lookup1D_UWORD",T_UWORD);
    typeDB.insert("Lookup1D_WORD",T_UWORD);
    typeDB.insert("Lookup1D_X_BOOLEAN",T_UBYTE);
    typeDB.insert("Lookup1D_X_BYTE",T_UBYTE);
    typeDB.insert("Lookup1D_X_FLOAT32_IEEE",T_FLOAT32_IEEE);
    typeDB.insert("Lookup1D_X_LONG",T_ULONG);
    typeDB.insert("Lookup1D_X_UBYTE",T_UBYTE);
    typeDB.insert("Lookup1D_X_ULONG",T_ULONG);
    typeDB.insert("Lookup1D_X_UWORD",T_UWORD);
    typeDB.insert("Lookup1D_X_WORD",T_UWORD);
    typeDB.insert("Lookup2D_BOOLEAN",T_UBYTE);
    typeDB.insert("Lookup2D_BYTE",T_UBYTE);
    typeDB.insert("Lookup2D_FLOAT32_IEEE",T_FLOAT32_IEEE);
    typeDB.insert("Lookup2D_LONG",T_ULONG);
    typeDB.insert("Lookup2D_UBYTE",T_UBYTE);
    typeDB.insert("Lookup2D_ULONG",T_ULONG);
    typeDB.insert("Lookup2D_UWORD",T_UWORD);
    typeDB.insert("Lookup2D_WORD",T_UWORD);
    typeDB.insert("Lookup2D_X_BOOLEAN",T_UBYTE);
    typeDB.insert("Lookup2D_X_BYTE",T_UBYTE);
    typeDB.insert("Lookup2D_X_FLOAT32_IEEE",T_FLOAT32_IEEE);
    typeDB.insert("Lookup2D_X_LONG",T_ULONG);
    typeDB.insert("Lookup2D_X_UBYTE",T_UBYTE);
    typeDB.insert("Lookup2D_X_ULONG",T_ULONG);
    typeDB.insert("Lookup2D_X_UWORD",T_UWORD);
    typeDB.insert("Lookup2D_X_WORD",T_UWORD);
    typeDB.insert("RL_X_FLOAT32_IEEE",T_FLOAT32_IEEE);
    typeDB.insert("RL_X_UWORD",T_UWORD);
    typeDB.insert("SBYTE",T_UBYTE);
    typeDB.insert("Scalar_BOOLEAN",T_UBYTE);
    typeDB.insert("Scalar_BYTE",T_UBYTE);
    typeDB.insert("Scalar_FLOAT32_IEEE",T_FLOAT32_IEEE);
    typeDB.insert("Scalar_LONG",T_ULONG);
    typeDB.insert("Scalar_SWORD",T_UWORD);
    typeDB.insert("Scalar_UBYTE",T_UBYTE);
    typeDB.insert("Scalar_ULONG",T_ULONG);
    typeDB.insert("Scalar_UWORD",T_UWORD);
    typeDB.insert("USHORT",T_UWORD);
    typeDB.insert("SWORD",T_UWORD);
    typeDB.insert(T_CURVE,T_CURVE);
    typeDB.insert(T_FLOAT32_IEEE,T_FLOAT32_IEEE);
    typeDB.insert(T_MAP,T_MAP);
    typeDB.insert(T_UBYTE,T_UBYTE);
    typeDB.insert(T_ULONG,T_ULONG);
    typeDB.insert(T_UWORD,T_UWORD);
    typeDB.insert(T_SLONG,T_SLONG);
}	//CA2LParser::initialize_typeDB
/*
  * @brief Comment for CA2LParser::parse_state_none
  * @param json
  * @param line
  * @param in
  * @param lineCount
  * @param jsonA2L
  * @param
  * @param typeDB
  * @param state
  * @param original
  * @return
  */
bool CA2LParser::parse_state_none(QJsonObject &json,
        QString &line,
        QTextStream &in,
        unsigned long &lineCount,
        QJsonObject &jsonA2L,
        const QMap <QString,QString> &typeDB,
        QString &state,
        QString &original,
        uint &indexCounter)
{
    if (line.contains(S_BEGIN_CHARACTERISTIC,Qt::CaseInsensitive))
    {
        parse_character(json,state,lineCount,in,typeDB,original,line,indexCounter);
    }
    else if (line.contains(S_BEGIN_MEASUREMENT,Qt::CaseInsensitive)){
        parse_measurement(json,state,lineCount,in,typeDB,original,line);
    }
    else if (line.contains(S_BEGIN_COMPU_METHOD,Qt::CaseInsensitive)){
        parse_compute(json,state,lineCount,in,original,line,jsonA2L);
    }	//beginCOMPU_METHOD
    else if (line.contains(S_BEGIN_MEMORY_REGION,Qt::CaseInsensitive)){
        bool skip = false;
        parse_memory(json,state,lineCount,in,original,line,jsonA2L,skip);
        if (skip)
        {
            return false;
        }
    }
    else if (line.contains(S_BEGIN_AXIS_PTS,Qt::CaseInsensitive)){
        parse_axis(json,state,lineCount,in,typeDB,original,line);
    }	//axis_ptr
    else
    {
        /****/
    }
    return true;
}	//CA2LParser::parse_state_none
/*
  * @brief Comment for CA2LParser::parse_state_begin_axis_descr
  * @param previous_state
  * @param first_axis_ref
  * @param state
  * @param line
  * @param json
  * @return
  */
void CA2LParser::parse_state_begin_axis_descr(QString previous_state,
        bool &first_axis_ref,
        QString &state,
        const QString &line,
        QJsonObject &json)
{
    if (line.contains("/end AXIS_DESCR",Qt::CaseInsensitive))
    {
        first_axis_ref = false;
        state = previous_state;
    }
    else if (line.startsWith("FORMAT ")){
        QString format = line.section(" ",1);
        static QRegularExpression reg5("^\"");
        static QRegularExpression reg6("\"$");
        format.replace(reg5,"");
        format.replace(reg6,"");
        json.insert(first_axis_ref?J_AXIS_X_FORMAT:J_AXIS_Y_FORMAT,format);
    }
    else if (line.startsWith("BYTE_ORDER ")){
        QString format = line.section(" ",1);
        json.insert(first_axis_ref?J_AXIS_X_BYTE_ORDER:J_AXIS_Y_BYTE_ORDER,format);
    }
    else if (line.startsWith("AXIS_PTS_REF ")){
        QString format = line.section(" ",1);
        json.insert(first_axis_ref?J_AXIS_X_AXIS_PTS:J_AXIS_Y_AXIS_PTS,format);
    }
    else
    {
        /***/
    }
}	//CA2LParser::parse_state_begin_axis_descr
/*
  * @brief Comment for CA2LParser::parse_state_axis_pts
  * @param state
  * @param jsonA2L
  * @param json
  * @param line
  * @return
  */
void CA2LParser::parse_state_axis_pts(QString &state,QJsonObject &jsonA2L,QJsonObject &json,const QString &line)
{
    if (line.contains("/end AXIS_PTS",Qt::CaseInsensitive))
    {
        QJsonObject type_set = jsonA2L.value(J_A2L_AXIS_REFS).toObject(
                QJsonObject());
        type_set.insert(json.value(J_A2L_NAME).toString(""),json);
        jsonA2L.insert(J_A2L_AXIS_REFS,type_set);
        state = "none";
    }
    else if (line.startsWith("FORMAT ")){
        QString format = line.section(" ",1);
        static QRegularExpression reg5("^\"");
        static QRegularExpression reg6("\"$");
        format.replace(reg5,"");
        format.replace(reg6,"");
        json.insert(J_A2L_FORMAT,format);
    }
    else if (line.startsWith("BYTE_ORDER ")){
        //
    }
    else if (line.startsWith("DEPOSIT ")){
        //
    }
    else
    {
        //
    }
}	//CA2LParser::parse_state_axis_pts
/**
  * @brief CA2LParser::parseFile
  * @param fileName
  * @param forceReparse
  * @param invalidA2LNames
  * @return
  */
QJsonObject CA2LParser::parseFile(cstr fileName,const QSet <QString> &invalidA2LNames,uint &indexCounter)
{
    QMap <QString,QString>typeDB;
    initialize_typeDB(typeDB);
    QFile file(fileName);
    EXPIF_F(file.open(QIODevice::ReadOnly | QFile::Text),
            CException_FileReadError,
            QObject::tr("File read error [%1]").arg(fileName))
    QString state = "none";
    QString previous_state = "";
    QString line;
    QTextStream in(&file);
    QJsonObject jsonA2L;
    QJsonObject json;
    unsigned long lineCount = 0;
    bool first_axis_ref = true;
    uint16_t a2L_counter = 0;
    bool ok = false;

    while (!in.atEnd())
    {
#if defined VALGRIND
        if (lineCount>100)
        {
            break;
        }
#endif
        QString original = "";
        line = readline(lineCount,in,original);
        if (line.isEmpty())
        {
            continue;
        }
        if (state=="none")
        {
            if (!parse_state_none(json,line,in,lineCount,jsonA2L,typeDB,state,original,indexCounter))
            {
                continue;
            }
        }	//state NONE
        else if ((state==S_BEGIN_CHARACTERISTIC) || (state==S_BEGIN_MEASUREMENT)){
            if (line.contains(S_END_CHARACTERISTIC,
                    Qt::CaseInsensitive) || line.contains(S_END_MEASUREMENT,Qt::CaseInsensitive))
            {
                state = "none";
                if (json.value(J_KEY_TYPE)=="XYZ")	//!!!
                {
                    //skip for now
                }
                else
                {
                    //add new object
                    if (invalidA2LNames.contains(json.value(
                            J_A2L_NAME).toString("")))
                    {
                        //skip
                    }
                    else
                    {
                        ++a2L_counter;
                        CHECKIF(a2L_counter>MAX_A2L_ENTRY,QString(
                                "A2L file contains more than allowed [%1] entities.").arg(MAX_A2L_ENTRY))
                        jsonA2L.insert(json.value(J_A2L_NAME).toString(
                                QString("Unknown_item l:%1").arg(
                                        lineCount)),json);
                    }
                }
                first_axis_ref = true;
            }
            else if (line.startsWith("ECU_ADDRESS ")){
                auto address_line = line.section(" ",1);
                static QRegularExpression reg5("^\"");
                static QRegularExpression reg6("\"$");
                address_line.replace(reg5,"");
                address_line.replace(reg6,"");
                EXPIF_F(AddressMatch.match(address_line).hasMatch(),CException_CAN_A2LError,
                        QObject::tr("Parse error in file[%1] line:[%2][%3] address:[%4]").arg(fileName).arg(
                                lineCount).arg(original).arg(address_line))
                json.insert(J_A2L_ADDRESS,address_line);
                json.insert(J_A2L_INDEX,QJsonValue(static_cast <int>(indexCounter)));
                indexCounter++;
            }
            else if (line.startsWith("FORMAT ")){
                auto format_line = line.section(" ",1);
                static QRegularExpression reg5("^\"");
                static QRegularExpression reg6("\"$");
                format_line.replace(reg5,"");
                format_line.replace(reg6,"");
                json.insert(J_A2L_FORMAT,format_line);
            }
            else if (line.startsWith("BYTE_ORDER ")){
                auto byte_order_line = line.section(" ",1);
                json.insert(J_A2L_BYTE_ORDER,byte_order_line);
            }
            else if (line.startsWith("BIT_MASK ")){
                QString format = line.section(" ",1);
                json.insert(J_A2L_BITMASK,format);
            }
            else if (line.startsWith("/begin AXIS_DESCR")){
                previous_state = state;
                state = S_BEGIN_AXIS_DESCR;
                /*line = */ readline(lineCount,in,original);//skip one line ???
                line = readline(lineCount,in,original);
                json.insert(first_axis_ref?J_AXIS_X_TYPE:J_AXIS_Y_TYPE,line);
                line = readline(lineCount,in,original);
                json.insert(first_axis_ref?J_AXIS_X_INPUTQUANTITY:J_AXIS_Y_INPUTQUANTITY,line);
                line = readline(lineCount,in,original);
                json.insert(first_axis_ref?J_AXIS_X_CONVERSION:J_AXIS_Y_CONVERSION,line);
                line = readline(lineCount,in,original);
                json.insert(first_axis_ref?J_AXIS_X_NUMBER_OF_POINTS:J_AXIS_Y_NUMBER_OF_POINTS,line.toInt(&ok));
                EXPIF_F(ok,CException_CAN_A2LError,QString("Parse error in a2l file line %1 %2").arg(
                        lineCount).arg(original))
                line = readline(lineCount,in,original);
                json.insert(first_axis_ref?J_AXIS_X_LOWER_LIMIT:J_AXIS_Y_LOWER_LIMIT,line);
                line = readline(lineCount,in,original);
                json.insert(first_axis_ref?J_AXIS_X_UPPER_LIMIT:J_AXIS_Y_UPPER_LIMIT,line);
            }
            else
            {
                /***/
            }
        }
        else if (state==S_BEGIN_AXIS_DESCR){
            parse_state_begin_axis_descr(previous_state,first_axis_ref,state,line,json);
        }
        else if (state==S_BEGIN_AXIS_PTS){
            parse_state_axis_pts(state,jsonA2L,json,line);
        }
        else
        {
            //
        }
    }

    return jsonA2L;
}	//CA2LParser::parseFile
/**
  * @brief CA2LParser::summarizeA2LObject
  * @param a2L
  */
void CA2LParser::summarizeA2LObject(QJsonObject &a2L)
{
    QStringList all_a2l_variables;
    QStringList all_a2l_curves;
    QStringList all_a2l_maps;
    QStringList all_a2l_single_values;
    QStringList all_a2l_nonfloats_values;
    QStringList all_a2l_floats_values;
    QStringList all_a2l_measurements;
    QStringList all_a2l_characteristics;
    QStringList all_dbc_variables;
    QStringList all_buffer_variables;
    QSet <QString>all_types;
    QStringList skip_keys{J_A2L_MEMORYREGIONS,J_A2L_COMPUTEMETHODS,J_A2L_AXIS_REFS};

    for (const QString &key:a2L.keys())
    {
        if (skip_keys.contains(key,Qt::CaseInsensitive))
        {
            continue;
        }
        QJsonObject json = a2L.value(key).toObject(QJsonObject());
        QString field_type = json.value(J_A2L_FIELDTYPE).toString("");
        QString data_type = json.value(J_A2L_DATATYPE).toString("");
        QString record_type = json.value(J_A2L_RECORDTYPE).toString("");
        auto source_type = static_cast <CEnums::A2L_SOURCE_TYPES>(json.value(J_A2L_SOURCE).toInt(
                static_cast <int>(CEnums::A2L)));
        all_types.insert(record_type);

        switch (source_type)
        {
           case CEnums::A2L:
            break;
           case CEnums::DBC:
            all_dbc_variables.append(key);
            break;
           case CEnums::BUFFER:
            all_buffer_variables.append(key);
            break;
           default:
            NOTIMPLEMENTED(QString("invalid source type %1").arg(source_type))
        }	//switch
        if (field_type==FIELD_TYPE_CHAR)
        {
            all_a2l_variables.append(key);
            all_a2l_characteristics.append(key);
        }
        else if (field_type==FIELD_TYPE_MEASURE){
            all_a2l_variables.append(key);
            all_a2l_measurements.append(key);
        }
        else if ((key==J_A2L_AXIS_REFS) || (key==J_A2L_COMPUTEMETHODS)){
            //skip
        }
        else
        {
            continue;
        }
        if (data_type==DATA_TYPE_MAP)
        {
            all_a2l_maps.append(key);
        }
        else if (data_type==DATA_TYPE_CURVE){
            all_a2l_curves.append(key);
        }
        else if ((data_type==DATA_TYPE_VALUE) || (field_type==FIELD_TYPE_MEASURE)){
            all_a2l_single_values.append(key);
        }
        else if ((key!=J_A2L_AXIS_REFS) && (key!=J_A2L_COMPUTEMETHODS)){
            ERRLOG << "No variable:" << key;
        }
        else
        {
            //
        }
        if (((record_type==T_UWORD) || (record_type==T_UBYTE) || (record_type==T_ULONG) || (record_type==T_SLONG) ||
             (record_type=="Scalar_BOOLEAN")) && ((data_type!=DATA_TYPE_MAP) && (data_type!=DATA_TYPE_CURVE)))
        {
            all_a2l_nonfloats_values.append(key);
        }
        else if ((record_type==T_FLOAT32_IEEE) && ((data_type!=T_MAP) && (data_type!=DATA_TYPE_CURVE))){
            all_a2l_floats_values.append(key);
        }
        else if ((data_type==T_MAP) || (data_type==T_CURVE)){
            //skip
            continue;
        }
        else if ((key!=J_A2L_AXIS_REFS) && (key!=J_A2L_COMPUTEMETHODS)){
            ERRLOG << "Variable not recognized:" << key << " Record_type:" << record_type << " Data type:" << data_type;
        }
        else
        {
            //skip
        }
    }

    all_a2l_variables.sort(Qt::CaseInsensitive);
    all_a2l_curves.sort(Qt::CaseInsensitive);
    all_a2l_maps.sort(Qt::CaseInsensitive);
    all_a2l_single_values.sort(Qt::CaseInsensitive);
    all_a2l_nonfloats_values.sort(Qt::CaseInsensitive);
    all_a2l_floats_values.sort(Qt::CaseInsensitive);
    all_a2l_measurements.sort(Qt::CaseInsensitive);
    all_a2l_characteristics.sort(Qt::CaseInsensitive);
    all_dbc_variables.sort(Qt::CaseInsensitive);
    all_dbc_variables.removeAll(QString("%1_%2").arg(JSON_SOURCE_NAME_DBC,J_A2L_ALL_PGNS_BY_ADDRESS));
    all_dbc_variables.removeAll(QString("%1").arg(J_A2L_ALL_PGNS_BY_ADDRESS));
    all_buffer_variables.sort(Qt::CaseInsensitive);
    a2L.insert(J_A2L_ALL_VARS,all_a2l_variables.join(","));
    a2L.insert(J_A2L_ALL_CURVES,all_a2l_curves.join(","));
    a2L.insert(J_A2L_ALL_MAPS,all_a2l_maps.join(","));
    a2L.insert(J_A2L_ALL_SINGLES,all_a2l_single_values.join(","));
    a2L.insert(J_A2L_ALL_NONFLOATS,all_a2l_nonfloats_values.join(","));
    a2L.insert(J_A2L_ALL_FLOATS,all_a2l_floats_values.join(","));
    a2L.insert(J_A2L_ALL_MEASUREMENTS,all_a2l_measurements.join(","));
    a2L.insert(J_A2L_ALL_CHARACTERS,all_a2l_characteristics.join(","));
    a2L.insert(J_A2L_ALL_DBC,all_dbc_variables.join(","));
    a2L.insert(J_A2L_ALL_BUFFER,all_buffer_variables.join(","));
#ifdef DEBUG_A2L
    //DEBUG << all_types.values().join("\n");
#endif
}	//CA2LParser::summarizeA2LObject
