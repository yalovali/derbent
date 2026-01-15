package tech.derbent.app.comments.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.utils.Check;

public final class CCommentInitializerService {

private static final Logger LOGGER = LoggerFactory.getLogger(CCommentInitializerService.class);
public static final String FIELD_NAME_COMMENTS = "comments";
public static final String SECTION_NAME_COMMENTS = "Comments";

public static void addCommentsSection(final CDetailSection detailSection, final Class<?> entityClass) throws Exception {
otNull(detailSection, "detailSection cannot be null");
otNull(entityClass, "entityClass cannot be null");
 {
.addScreenLine(CDetailLinesService.createSection(SECTION_NAME_COMMENTS));
.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, FIELD_NAME_COMMENTS));
catch (final Exception e) {
adding Comments section for {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
e;
CCommentInitializerService() {
}
}
