package org.qing.object;

import java.io.Serializable;

public class Context implements Serializable {

		private Integer id;
		private Integer rank;
		private Integer processId;
		private Integer versionId;
		private String contextFilePath;
		private String tempFilePath;
		
		public Integer getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = id;
		}
		public Integer getRank() {
			return rank;
		}
		public void setRank(Integer rank) {
			this.rank = rank;
		}
		public Integer getProcessId() {
			return processId;
		}
		public void setProcessId(Integer processId) {
			this.processId = processId;
		}
		public Integer getVersionId() {
			return versionId;
		}
		public void setVersionId(Integer versionId) {
			this.versionId = versionId;
		}
		public String getContextFilePath() {
			return contextFilePath;
		}
		public void setContextFilePath(String contextFilePath) {
			this.contextFilePath = contextFilePath;
		}
		public String getTempFilePath() {
			return tempFilePath;
		}
		public void setTempFilePath(String tempFilePath) {
			this.tempFilePath = tempFilePath;
		}
		
		
}
