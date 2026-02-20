/*********************************************************
  * Project Name    : ECEMTAG DIAGNOSTIC TOOL
  * Project Code    : ST
  * Author          : yalovali@gmail.com
  * Creation Date   : 12.05.2023
  * Description     : Diagnostic tool application for ECU/TCU
  **********************************************************
  * Purpose         : Header file for CA2LParser of ST project
  **********************************************************/
/*******************************************/
#include "defines.h"
/* disable compiler warnings on external includes of QT. Internal includes also added to reduce multiple warnings */
DISABLE_ALL_WARNINGS_BEGIN
#include <QFile>
#include <QJsonArray>

#include "ca2lparser.h"
#include "cauxillary.h"
#include "cexception.h"
DISABLE_ALL_WARNINGS_END
/*******************************************/
/**
  * @brief CA2LParser::save Save json object for debug purposes
  * @param filepath File name to save
  * @return True on success
  */
void CA2LParser::save(const QJsonObject &json,cstr fileName)
{
    //file location from auxillary class
    QFile file(fileName);
    //try to open it
    EXPIF_F(file.open(QFile::WriteOnly),CException_FileWriteError,QObject::tr(
            "A2L file cannot be saved:[%1]").arg(fileName))
    //write to file
    file.write(QJsonDocument(json).toJson());
    file.close();
}
/**
  * @brief comment for CA2LParser::printKeysWithValue
  * @param json
  * @param key
  * @param value
  * @return
  */
void CA2LParser::printKeysWithValue(const QJsonObject &json,QString key,cstr value)
{
    for (const QString &child:json.keys())
    {
        if (json.value(child).toObject().value(key).toString()==value)
        {
            DEBUG << "Key:" << child << " Name:" << json.value(child).toObject().value(
                    J_A2L_NAME).toString();
        }
    }
}
/**
  * @brief comment for CA2LParser::reverse_calculateConversions
  * @param input
  * @param a
  * @param b
  * @return
  */
float CA2LParser::reverse_calculateConversions(float input,float a,float b,float c,float d,float e,float f)
{
    //TODO DO THE REAL CONVERSION !!!!
    EXPIF(a!=0 || d!=0 || e!=0,CException_CAN_A2LError,
            QString("Not reversible conversion values a:%1 b:%2 c:%3 d:%4 e:%5 f:%6").arg(a).arg(b).arg(c).arg(d).arg(
                    e).arg(f))
    float result = (input-c) / f;
    return result;
}
/**
  * @brief comment for CA2LParser::reverse_calculateConversions
  * @param a2L
  * @param name
  * @param input
  * @return
  */
float CA2LParser::reverse_calculateConversions(const QJsonObject &a2L,cstr name,float input)
{
    const QJsonObject &json = a2L.value(name).toObject(QJsonObject());
    if (json==QJsonObject())
    {
        ERRLOG << QString("invalid conversion method. [%1]").arg(name);
        return input;
    }
    float result = 0;
    QString conversion_method = json.value(J_A2L_CONVERSION).toString(NO_COMPU_METHOD);
    if (conversion_method==NO_COMPU_METHOD)
    {
        return input;
    }
    QJsonObject conversion = a2L.value(J_A2L_COMPUTEMETHODS).toObject(QJsonObject()).value(
            conversion_method).toObject();
    if (conversion.isEmpty())
    {
        ERRLOG << QString("invalid conversion method. [%1]").arg(conversion_method);
        return input;
    }
    QString coefficients = conversion.value(J_A2L_COEFFICIENTS).toString();
    QStringList values = coefficients.split(" ");
    if (values[0]!="COEFFS")
    {
        ERRLOG << "Not implemented coeffs" << values[0];
        return -1;
    }
    bool ok = false;
    float a = values[1].toFloat(&ok);
    if (!ok)
    {
        ERRLOG << "Invalid coeffs value" << coefficients;
    }
    float b = values[2].toFloat(&ok);
    if (!ok)
    {
        ERRLOG << "Invalid coeffs value" << coefficients;
    }
    float c = values[3].toFloat(&ok);
    if (!ok)
    {
        ERRLOG << "Invalid coeffs value" << coefficients;
    }
    float d = values[4].toFloat(&ok);
    if (!ok)
    {
        ERRLOG << "Invalid coeffs value" << coefficients;
    }
    float e = values[5].toFloat(&ok);
    if (!ok)
    {
        ERRLOG << "Invalid coeffs value" << coefficients;
    }
    float f = values[6].toFloat(&ok);
    if (!ok)
    {
        ERRLOG << "Invalid coeffs value" << coefficients;
    }
    result = reverse_calculateConversions(input,a,b,c,d,e,f);
    return result;
}	//CA2LParser::reverse_calculateConversions
/**
  * @brief comment for CA2LParser::getConversionCoeffs
  * @param a2L
  * @param conversion_method
  * @param a
  * @param b
  * @param units
  * @return
  */
void CA2LParser::getConversionCoeffs_aux(const QJsonObject &conversion,
        float &a,
        float &b,
        float &c,
        float &d,
        float &e,
        float &f,
        QString &units)
{
    a = 0;
    b = 1;
    c = 0;
    d = 0;
    e = 0;
    f = 1;
    units = "";
    units = conversion.value(J_A2L_UNITS).toString("");
    QString coefficients = conversion.value(J_A2L_COEFFICIENTS).toString();
    QStringList values = coefficients.split(" ");
    if (values[0]=="COEFFS")
    {
        //ok we will parse this
    }
    else if (values[0]=="COMPU_TAB_REF"){
        //machine specific DB table lookup. Not implemented yet.
        ERRLOG << "Not implemented coeffs" << values[0];
        return;
    }
    else
    {
        //FIX IT
    }
    /*
      * This keyword is used to specify coefficients a, b, c, d, e, f for the fractional rational function of the following type: f(x)=(axx + bx + c) / (dxx + ex + f)
      */
    bool ok = false;
    if (values.count()>6)
    {
        /*
           a = values[6].toFloat(&ok); //OLD
           CHECKIF_F(ok,QString("Invalid coeffs value %1").arg(coefficients))
           b = values[2].toFloat(&ok); //OLD
           CHECKIF_F(ok,QString("Invalid coeffs value %1").arg(coefficients))
          */
        a = values[1].toFloat(&ok);	//OLD
        CHECKIF_F(ok,QString("Invalid coeffs value %1").arg(coefficients))
        b = values[2].toFloat(&ok);	//OLD
        CHECKIF_F(ok,QString("Invalid coeffs value %1").arg(coefficients))
        c = values[3].toFloat(&ok);	//OLD
        CHECKIF_F(ok,QString("Invalid coeffs value %1").arg(coefficients))
        d = values[4].toFloat(&ok);	//OLD
        CHECKIF_F(ok,QString("Invalid coeffs value %1").arg(coefficients))
        e = values[5].toFloat(&ok);	//OLD
        CHECKIF_F(ok,QString("Invalid coeffs value %1").arg(coefficients))
        f = values[6].toFloat(&ok);	//OLD
        CHECKIF_F(ok,QString("Invalid coeffs value %1").arg(coefficients))
    }
    else
    {
        a = 0;
        b = 1;
        c = 0;
        d = 0;
        e = 0;
        f = 1;
    }
}	//CA2LParser::getConversionCoeffs_aux
/**
  * @brief comment for CA2LParser::getConversionCoeffs
  * @param a2L
  * @param conversion_method
  * @param a
  * @param b
  * @param units
  * @return
  */
void CA2LParser::getConversionCoeffs(const QJsonObject &a2L,
        cstr &conversion_method,
        float &a,
        float &b,
        float &c,
        float &d,
        float &e,
        float &f,
        QString &units)
{
    const QJsonObject &conversion = a2L.value(J_A2L_COMPUTEMETHODS).toObject(
            QJsonObject()).value(
            conversion_method).toObject();
    getConversionCoeffs_aux(conversion,a,b,c,d,e,f,units);
}
/**
  * @brief comment for CA2LParser::getConversionCoeffs
  * @param a2L
  * @param conversion_method
  * @param a
  * @param b
  * @param units
  * @return
  */
void CA2LParser::getConversionCoeffs(const QJsonObject &a2L,
        const QJsonObject &a2L_json,
        QString &conversion_method,
        float &a,
        float &b,
        float &c,
        float &d,
        float &e,
        float &f,
        QString &units,
        ushort &decimals,
        ushort &digits)
{
    decimals = DEFAULT_DECIMALS_OF_CONVERSION;
    digits = DEFAULT_DIGITS_OF_CONVERSION;
    a = 0;
    b = 1;
    c = 0;
    d = 0;
    e = 0;
    f = 1;
    conversion_method = a2L_json.value(J_A2L_CONVERSION).toString();
    RETURN_VOID_IF(conversion_method.isEmpty())	//no problem to have an empty conversion
    RETURN_VOID_IF(conversion_method==NO_COMPU_METHOD)	//no conversion
    const QJsonObject &conversion = a2L.value(J_A2L_COMPUTEMETHODS).toObject(
            QJsonObject()).value(
            conversion_method).toObject();
    CHECKIF(conversion.isEmpty(),QString("invalid conversion method. [%1]").arg(
            conversion_method))
    digits = displayFormat_digits(conversion,a2L_json);
    decimals = displayFormat_decimal(conversion,a2L_json);
    getConversionCoeffs_aux(conversion,a,b,c,d,e,f,units);
    auto recordtype = a2L_json.value(J_A2L_RECORDTYPE).toString();
    //DISABLE_SONAR we check decimals of a, can be rewritten using truncate ? yy
    if ((recordtype!=T_FLOAT32_IEEE) && ((a-static_cast <float>(static_cast <int>(a)))==0.0f))
    {
        //cannot be non zero
        decimals = 0;
    }
}	//CA2LParser::getConversionCoeffs
/**
  * @brief comment for CA2LParser::calculateConversions
  * @param input
  * @param a
  * @param b
  * @return
  */
float CA2LParser::calculateConversions(float input,float a,float b,float c,float d,float e,float f)
{
    float result = input;
    if ((a==0) && (b==1) && (c==0) && (d==0) && (e==0) && (f==1))
    {
        return result;
    }
    //for ecemtag ATIVISION the formula is wrong
    if (f!=1)
    {
        result = (input * f)+c;
    }
    else
    {
        //this is correct version
        result = ((input * input * a)+(input * b)+c) / ((input * input * d)+(input * e)+f);
    }
    return result;
}
/**
  * @brief comment for CA2LParser::calculateConversions
  * @param input
  * @param a
  * @param b
  * @return
  */
float CA2LParser::calculateConversions(ushort input,float a,float b,float c,float d,float e,float f)
{
    if ((a==0) && (b==1) && (c==0) && (d==0) && (e==0) && (f==1))
    {
        return input;
    }
    return calculateConversions(static_cast <float>(input),a,b,c,d,e,f);
}
/**
  * @brief comment for CA2LParser::calculateConversions
  * @param input
  * @param a
  * @param b
  * @return
  */
float CA2LParser::calculateConversions(char input,float a,float b,float c,float d,float e,float f)
{
    if ((a==0) && (b==1) && (c==0) && (d==0) && (e==0) && (f==1))
    {
        return input;
    }
    return calculateConversions(static_cast <float>(input),a,b,c,d,e,f);
}
/**
  * @brief comment for CA2LParser::calculateConversions
  * @param input
  * @param a
  * @param b
  * @return
  */
float CA2LParser::calculateConversions(uint32_t input,float a,float b,float c,float d,float e,float f)
{
    if ((a==0) && (b==1) && (c==0) && (d==0) && (e==0) && (f==1))
    {
        return static_cast <float>(input);
    }
    return calculateConversions(static_cast <float>(input),a,b,c,d,e,f);
}
/**
  * @brief comment for CA2LParser::calculateConversions
  * @param a2L
  * @param conversion_method
  * @param input
  * @return
  */
float CA2LParser::calculateConversions(const QJsonObject &a2L,cstr conversion_method,float input)
{
    //dont call this function with qstring. get a/b already ready in the entityclass
    float a = 0;
    float b = 0;
    float c = 0;
    float d = 0;
    float e = 0;
    float f = 0;
    QString units;
    CA2LParser::getConversionCoeffs(a2L,conversion_method,a,b,c,d,e,f,units);
    return calculateConversions(input,a,b,c,d,e,f);
}
/**
  * @brief comment for CA2LParser::displayFormat_digits
  * @param conversion
  * @param json
  * @return
  */
ushort CA2LParser::displayFormat_digits(const QJsonObject &conversion,const QJsonObject &json)
{
    //ushort digit = DEFAULT_DIGITS_OF_CONVERSION;
    QString format = conversion.value(J_A2L_FORMAT).toString("");
    format = json.value(J_A2L_FORMAT).toString(format);	//override conversion method
    format.remove("%");
    bool ok = true;
    ushort digit = format.section(".",0,0).toUShort(&ok);
    if (!ok)
    {
        ERRLOG << QString("invalid format [%1]").arg(conversion.value(
                J_A2L_FORMAT).toString(
                        ""));
        return DEFAULT_DIGITS_OF_CONVERSION;//default value
    }
    return digit;
}
/**
  * @brief comment for CA2LParser::displayFormat_decimal
  * @param conversion
  * @param json
  * @return
  */
ushort CA2LParser::displayFormat_decimal(const QJsonObject &conversion,const QJsonObject &json)
{
    //ushort digit = DEFAULT_DECIMALS_OF_CONVERSION;
    auto format = conversion.value(J_A2L_FORMAT).toString("");
    format = json.value(J_A2L_FORMAT).toString(format);	//override conversion method format
    format.remove("%");
    bool ok = true;
    ushort digit = format.section(".",1,1).toUShort(&ok);
    if (!ok)
    {
        ERRLOG << QString("invalid format [%1]").arg(conversion.value(
                J_A2L_FORMAT).toString(
                        ""));
        return DEFAULT_DECIMALS_OF_CONVERSION;	//default value
    }
    return digit;
}
/**
  * @brief comment for CA2LParser::displayFormat_digits
  * @param a2L
  * @param name
  * @return
  */
ushort CA2LParser::displayFormat_digits(const QJsonObject &a2L,cstr name)
{
    if (name.isEmpty())
    {
        return DEFAULT_DIGITS_OF_CONVERSION;
    }
    const QJsonObject &json = a2L.value(name).toObject(QJsonObject());
    if (json==QJsonObject())
    {
        ERRLOG << QString("invalid a2L name. [%1]").arg(name);
        return DEFAULT_DIGITS_OF_CONVERSION;
    }
    QString conversion_method = json.value(J_A2L_CONVERSION).toString(NO_COMPU_METHOD);
    auto type_set = a2L.value(J_A2L_COMPUTEMETHODS).toObject(QJsonObject());
    QJsonObject conversion = type_set.value(conversion_method).toObject();
    return displayFormat_digits(conversion,json);
}
/**
  * @brief comment for CA2LParser::displayFormat_decimal
  * @param a2L
  * @param name
  * @return
  */
ushort CA2LParser::displayFormat_decimal(const QJsonObject &a2L,cstr name)
{
    if (name.isEmpty())
    {
        return DEFAULT_DECIMALS_OF_CONVERSION;
    }
    const QJsonObject &json = a2L.value(name).toObject(QJsonObject());
    if (json==QJsonObject())
    {
        ERRLOG << QString("invalid a2L name. [%1]").arg(name);
        return DEFAULT_DECIMALS_OF_CONVERSION;
    }
    QString conversion_method = json.value(J_A2L_CONVERSION).toString(NO_COMPU_METHOD);
    auto type_set = a2L.value(J_A2L_COMPUTEMETHODS).toObject(QJsonObject());
    QJsonObject conversion = type_set.value(conversion_method).toObject();
    return displayFormat_decimal(conversion,json);
}
/**
  * @brief comment for CA2LParser::trancateFormat_Value
  * @param a2L
  * @param name
  * @param input
  * @return
  */
double CA2LParser::trancateFormat_Value(double input,ushort decimal)
{
    double result = 0.0;
    auto divisor = pow(10,decimal);
    result = std::floor(input * divisor);
    result = result / divisor;
    return result;
}
/**
  * @brief comment for CA2LParser::trancateFormat_Value
  * @param a2L
  * @param name
  * @param input
  * @return
  */
float CA2LParser::trancateFormat_Value(float input,ushort decimal)
{
    float result = 0.0;
    auto divisor = pow(10,decimal);
#if defined __linux__ || defined COMPILER_MINGW
    result = floorf(static_cast <float>(input * divisor));
#else
    result = std::floorf(static_cast <float>(input * divisor));
#endif
    result = static_cast <float>(result / divisor);
    return result;
}
/*
  * @brief Comment for CA2LParser::mergeToA2LFormatJ1939
  * @param a2L TODO
  * @param source TODO
  * @return
  */
void CA2LParser::mergeToA2LFormatJ1939(QJsonObject &a2L,const QJsonObject &source)
{
    foreach(QString key,source.keys())
    {
        QJsonObject message = source.value(key).toObject();
        foreach(QJsonValue value,message.value(J_KEY_CHILDREN).toArray())
        {
            QString signal_name = value.toObject().value(J_A2L_NAME).toString();
            if (signal_name.isEmpty())
            {
                ERRLOG << "Signal name is empty for:" << value.toString();
                continue;
            }
            a2L.insert(signal_name,value);
        }
    }
    a2L.insert(J_A2L_ALL_PGNS_BY_ADDRESS,source.value(J_A2L_ALL_PGNS_BY_ADDRESS).toObject());
}
/**
  * @brief comment for CA2LParser::mergeDBCtoA2LFormat
  * @param A2L
  * @param DBC
  * @return
  */
void CA2LParser::mergeToA2LFormat(QJsonObject &a2L,const QJsonObject &source,cstr prefix,int source_tag)
{
    RETURN_VOID_IF(source.isEmpty());

    for (const QString &key:source.keys())
    {
        if (key==J_A2L_COMPUTEMETHODS)
        {
            QJsonObject dest = a2L.value(key).toObject(QJsonObject());
            //no prefix !!!
            mergeToA2LFormat(dest,source.value(key).toObject(),"",source_tag);
            a2L.insert(key,dest);
        }
        else
        {
            QJsonObject json = source.value(key).toObject(QJsonObject());
            json.insert(J_A2L_SOURCE,source_tag);
            if (prefix.isEmpty())
            {
                a2L.insert(key,json);
            }
            else
            {
                a2L.insert(QString("%1_%2").arg(prefix,key),json);
            }
        }
    }
}	//CA2LParser::mergeToA2LFormat
