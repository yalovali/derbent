/*********************************************************
  * Project Name    : ECEMTAG DIAGNOSTIC TOOL
  * Project Code    : ST
  * Author          : yalovali@gmail.com
  * Creation Date   : 12.05.2023
  * Description     : Diagnostic tool application for ECU/TCU
  **********************************************************
  * Purpose         : Header file for CA2LParser of ST project
  **********************************************************/
#ifndef CA2LPARSER_H
#define CA2LPARSER_H
/*******************************************/
//inlude section
#include "defines.h"
/* disable compiler warnings on external includes of QT. Internal includes also added to reduce multiple warnings */
DISABLE_ALL_WARNINGS_BEGIN
#include <QJsonDocument>
#include <QJsonObject>
#include <QRegularExpression>
#include <QTextStream>

#include "cparser.h"
DISABLE_ALL_WARNINGS_END
/*******************************************/

/**
  * @brief The CA2LParser class
  * This static function only class is used to parse A2L file into Json file.
  * Not all variable types are supported, check debug messages
  */
class CA2LParser:public CParser
{
    /*******************************************/
    public:
        static QJsonObject parseFile(cstr fileName,const QSet <QString> &invalidA2LNames,uint &indexCounter);
        static void save(const QJsonObject &json,cstr fileName);
        static void printKeysWithValue(const QJsonObject &json,QString key,cstr value);
        static float calculateConversions(const QJsonObject &a2L,cstr conversion_method,float input);
        static float calculateConversions(float input,float a,float b,float c,float d,float e,float f);
        static float calculateConversions(ushort input,float a,float b,float c,float d,float e,float f);
        static float calculateConversions(char input,float a,float b,float c,float d,float e,float f);
        static float calculateConversions(uint32_t input,float a,float b,float c,float d,float e,float f);
        static void getConversionCoeffs(const QJsonObject &a2L,
                                        cstr &conversion_method,
                                        float &a,
                                        float &b,
                                        float &c,
                                        float &d,
                                        float &e,
                                        float &f,
                                        QString &units);
        static void getConversionCoeffs(const QJsonObject &a2L,
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
                                        ushort &digits);
        static void getConversionCoeffs_aux(const QJsonObject &conversion,
                                            float &a,
                                            float &b,
                                            float &c,
                                            float &d,
                                            float &e,
                                            float &f,
                                            QString &units);
        static float reverse_calculateConversions(const QJsonObject &a2L,cstr name,float input);
        static float reverse_calculateConversions(float input,float a,float b,float c,float d,float e,float f);
        static ushort displayFormat_decimal(const QJsonObject &a2L,cstr name);
        static ushort displayFormat_decimal(const QJsonObject &conversion,const QJsonObject &json);
        static ushort displayFormat_digits(const QJsonObject &a2L,cstr name);
        static ushort displayFormat_digits(const QJsonObject &conversion,const QJsonObject &json);
        static double trancateFormat_Value(double input,ushort decimal);
        static float trancateFormat_Value(float input,ushort decimal);
        static void mergeToA2LFormatJ1939(QJsonObject &a2L,const QJsonObject &source);
        static void mergeToA2LFormat(QJsonObject &a2L,const QJsonObject &source,cstr prefix,int source_tag);
        static void summarizeA2LObject(QJsonObject &a2L);

    private:
        const static QRegularExpression AddressMatch;
        const static QRegularExpression HexMatch;
        static bool parse_state_none(QJsonObject &json,
                                     QString &line,
                                     QTextStream &in,
                                     unsigned long &lineCount,
                                     QJsonObject &jsonA2L,
                                     const QMap <QString,QString> &typeDB,
                                     QString &state,
                                     QString &original,
                                     uint &indexCounter);
        static void parse_state_begin_axis_descr(QString previous_state,
                                                 bool &first_axis_ref,
                                                 QString &state,
                                                 const QString &line,
                                                 QJsonObject &json);
        static void parse_state_axis_pts(QString &state,QJsonObject &jsonA2L,QJsonObject &json,const QString &line);
        static void parse_character(QJsonObject &json,
                                    QString &state,
                                    unsigned long &lineCount,
                                    QTextStream &in,
                                    const QMap <QString,QString> &typeDB,
                                    QString &original,
                                    QString &line,
                                    uint &indexCounter);
        static void parse_measurement(QJsonObject &json,
                                      QString &state,
                                      unsigned long &lineCount,
                                      QTextStream &in,
                                      const QMap <QString,QString> &typeDB,
                                      QString &original,
                                      QString &line);
        static void parse_compute(QJsonObject &json,
                                  QString &state,
                                  unsigned long &lineCount,
                                  QTextStream &in,
                                  QString &original,
                                  QString &line,
                                  QJsonObject &jsonA2L);
        static void parse_memory(QJsonObject &json,
                                 QString &state,
                                 unsigned long &lineCount,
                                 QTextStream &in,
                                 QString &original,
                                 QString &line,
                                 QJsonObject &jsonA2L,
                                 bool &skip);
        static void parse_axis(QJsonObject &json,QString &state,unsigned long &lineCount,QTextStream &in,
                               const QMap <QString,QString>
                               &typeDB,QString &original,QString &line);
        static bool getToken_Commented(cstr line,QJsonObject &json,cstr comment,cstr key);
        static void initialize_typeDB(QMap <QString,QString> &typeDB);
};
#endif	//CA2LPARSER_H
