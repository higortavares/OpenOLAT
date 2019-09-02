/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.learningpath.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathRoles;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.evaluation.AccessEvaluator;
import org.olat.course.learningpath.evaluation.DurationEvaluatorProvider;
import org.olat.course.learningpath.evaluation.LearningPathEvaluator;
import org.olat.course.learningpath.evaluation.ObligationEvaluatorProvider;
import org.olat.course.learningpath.evaluation.StatusEvaluatorProvider;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 26 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathTreeModelBuilder {
		
	private final CourseNode rootNode;
	private final ScoreAccounting scoreAccounting;
	private final LearningPathRoles roles;
	
	private LearningPathService learningPathService;
	
	public static LearningPathTreeModelBuilder builder(UserCourseEnvironment userCourseEnv) {
		CourseNode rootNode = userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
		ScoreAccounting scoreAccounting = userCourseEnv.getScoreAccounting();
		LearningPathRoles roles = LearningPathRoles.of(userCourseEnv);
		
		LearningPathTreeModelBuilder builder = new LearningPathTreeModelBuilder(rootNode, scoreAccounting, roles);

		return builder;
	}

	private LearningPathTreeModelBuilder(CourseNode rootNode, ScoreAccounting scoreAccounting, LearningPathRoles roles) {
		this.rootNode = rootNode;
		this.scoreAccounting = scoreAccounting;
		this.roles = roles;
	}
	
	public GenericTreeModel create() {
		learningPathService = CoreSpringFactory.getImpl(LearningPathService.class);
		
		GenericTreeModel treeModel = createTreeModel();
		
		StatusEvaluatorProvider statusEvaluatorProvider = learningPathService.getStatusEvaluatorProvider();
		ObligationEvaluatorProvider obligationEvaluatorProvider = learningPathService.getObligationEvaluatorProvider();
		DurationEvaluatorProvider durationEvaluatorProvider = learningPathService.getDurationEvaluatorProvider();
		AccessEvaluator accessEvaluator = learningPathService.getAccessEvaluator();

		LearningPathEvaluator.builder()
				.refreshObligation(obligationEvaluatorProvider)
				.refreshStatus(statusEvaluatorProvider, scoreAccounting)
				.refreshDuration(durationEvaluatorProvider)
				.refreshAccess(accessEvaluator, roles)
				.build()
				.refresh(treeModel);
		
		return treeModel;
	}

	private GenericTreeModel createTreeModel() {
		int recursionLevel = 0;
		LearningPathTreeNode rootLearningPathNode = new LearningPathTreeNode(rootNode, recursionLevel);
		GenericTreeModel treeModel = new GenericTreeModel();
		treeModel.setRootNode(rootLearningPathNode);
		addChildren(rootLearningPathNode, recursionLevel);
		return treeModel;
	}

	private void addChildren(LearningPathTreeNode parent, int parentLevel) {
		int childCount = parent.getCourseNode().getChildCount();
		int recursionLevel = parentLevel + 1;
		for (int childIndex = 0; childIndex < childCount; childIndex++) {
			CourseNode child = (CourseNode)parent.getCourseNode().getChildAt(childIndex);
			LearningPathTreeNode learningPathNode = createLearningPathTreeNode(child, recursionLevel);
			parent.addChild(learningPathNode);
			addChildren(learningPathNode, recursionLevel);
		}
	}

	private LearningPathTreeNode createLearningPathTreeNode(CourseNode courseNode, int recursionLevel) {
		LearningPathTreeNode learningPathTreeNode = new LearningPathTreeNode(courseNode, recursionLevel);
		
		LearningPathConfigs configs = learningPathService.getConfigs(courseNode);
		learningPathTreeNode.setDuration(configs.getDuration());
		
		return learningPathTreeNode;
	}

}