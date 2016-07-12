/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.sla;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.modeshape.jcr.api.JcrTools;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;
import com.thinkbiganalytics.metadata.modeshape.sla.JcrServiceLevelAgreement.SlaId;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup.Condition;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID;
import com.thinkbiganalytics.metadata.sla.spi.ObligationBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ObligationGroupBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;

/**
 *
 * @author Sean Felten
 */
public class JcrServiceLevelAgreementProvider extends BaseJcrProvider<ServiceLevelAgreement, ServiceLevelAgreement.ID> implements ServiceLevelAgreementProvider {
    
    public static final String SLA_PATH = "/metadata/sla";
    
    private final JcrTools jcrTools = new JcrTools();

    @Override
    public Class<? extends ServiceLevelAgreement> getEntityClass() {
        return JcrServiceLevelAgreement.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrServiceLevelAgreement.class;
    }

    @Override
    public String getNodeType() {
        return "tba:sla";
    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider#resolve(java.io.Serializable)
     */
    @Override
    public ID resolve(Serializable ser) {
        return resolveId(ser);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider#getAgreements()
     */
    @Override
    public List<ServiceLevelAgreement> getAgreements() {
        try {
            Session session = getSession();
            Node slasNode = session.getNode(SLA_PATH);
            @SuppressWarnings("unchecked")
            Iterator<Node> itr = (Iterator<Node>) slasNode.getNodes();
            
            return Lists.newArrayList(Iterators.transform(itr, (slaNode) -> {
                return JcrUtil.createJcrObject(slaNode, JcrServiceLevelAgreement.class);
            }));
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the obligation nodes", e);
        }
        
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider#getAgreement(com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID)
     */
    @Override
    public ServiceLevelAgreement getAgreement(ID id) {
        try {
            Session session = getSession();
            SlaId slaId = (SlaId) id;
            return new JcrServiceLevelAgreement(session.getNodeByIdentifier(slaId.getIdValue()));
        } catch (ItemNotFoundException e) { 
            return null;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the SLA node", e);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider#findAgreementByName(java.lang.String)
     */
    @Override
    public ServiceLevelAgreement findAgreementByName(String slaName) {
        String query =  "SELECT * FROM ["+getNodeType()+"] as sla WHERE sla.["+JcrPropertyConstants.TITLE+"] = '"+slaName+"'";
        return findFirst(query);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider#removeAgreement(com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID)
     */
    @Override
    public boolean removeAgreement(ID id) {
        try {
            Session session = getSession();
            SlaId slaId = (SlaId) id;
            Node slaNode = session.getNodeByIdentifier(slaId.getIdValue());
            slaNode.remove();
            return true;
        } catch (ItemNotFoundException e) { 
            return false;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the SLA node", e);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider#builder()
     */
    @Override
    public ServiceLevelAgreementBuilder builder() {
        try {
            Session session = getSession();
            Node slasNode = session.getNode(SLA_PATH);
            Node slaNode = slasNode.addNode("sla-" + UUID.randomUUID(), "tba:sla");
            
            return builder(slaNode);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create an sla node", e);
        }
    }

    /**
     * Returns a builder that constructs an SLA rooted by the given node.  This method is exposed to support
     * other JCR-based providers that may construct object that have embedded SLA's that are not managed by
     * this provider.
     * @param slaNode the root node of the SLA
     * @return a builder to construct the sla
     * @throws RepositoryException
     */
    public ServiceLevelAgreementBuilder builder(Node slaNode) throws RepositoryException {
        return new SLABuilderImpl(slaNode);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider#builder(com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID)
     */
    @Override
    public ServiceLevelAgreementBuilder builder(ID id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ID resolveId(Serializable ser) {
        if (ser instanceof JcrServiceLevelAgreement.SlaId) {
            return (JcrServiceLevelAgreement.SlaId) ser;
        } else {
            return new JcrServiceLevelAgreement.SlaId(ser);
        }
    }

    

    private class SLABuilderImpl implements ServiceLevelAgreementBuilder {

        private Node slaNode;
        
        private String name;
        private String description;

        public SLABuilderImpl(Node node) throws RepositoryException {
            this.slaNode = node;
        }

        @Override
        public ServiceLevelAgreementBuilder name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public ServiceLevelAgreementBuilder description(String description) {
            this.description = description;
            return this;
        }

        @Override
        public ServiceLevelAgreementBuilder obligation(Obligation obligation) {
            // TODO This isn't going to work in the current JCR implementation.  Perhaps it should not be supported at all in the builder.
            throw new UnsupportedOperationException();
        }

        @Override
        public ObligationBuilder<ServiceLevelAgreementBuilder> obligationBuilder() {
            try {
                Node groupNode = null;
                
                if (this.slaNode.hasProperty(JcrServiceLevelAgreement.DEFAULT_GROUP)) {
                    groupNode = this.slaNode.getProperty(JcrServiceLevelAgreement.DEFAULT_GROUP).getNode();
                } else {
                    groupNode = this.slaNode.addNode(JcrServiceLevelAgreement.GROUPS, JcrServiceLevelAgreement.GROUP_TYPE);
                    this.slaNode.setProperty(JcrServiceLevelAgreement.DEFAULT_GROUP, groupNode);
                }
                
                Node obNode = groupNode.addNode(JcrObligationGroup.OBLIGATIONS, JcrObligationGroup.OBLIGATION_TYPE);
                
                return new ObligationBuilderImpl<ServiceLevelAgreementBuilder>(obNode, this);
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Failed to build the obligation node", e);
            }
        }
        
        @Override
        public ObligationBuilder<ServiceLevelAgreementBuilder> obligationBuilder(Condition condition) {
            try {
                Node groupNode = this.slaNode.getProperty(JcrServiceLevelAgreement.DEFAULT_GROUP).getNode();
                groupNode.setProperty(JcrObligationGroup.CONDITION, condition.name());
                Node obNode = groupNode.addNode(JcrObligationGroup.OBLIGATIONS, JcrObligationGroup.OBLIGATION_TYPE);
                
                return new ObligationBuilderImpl<ServiceLevelAgreementBuilder>(obNode, this);
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Failed to build the obligation group node", e);
            }
        }

        @Override
        public ObligationGroupBuilder obligationGroupBuilder(Condition condition) {
            try {
                Node groupNode = this.slaNode.addNode(JcrServiceLevelAgreement.GROUPS, JcrServiceLevelAgreement.GROUP_TYPE);
                    
                return new ObligationGroupBuilderImpl(groupNode, condition, this);
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Failed to build the obligation group node", e);
            }
        }

        @Override
        public ServiceLevelAgreement build() {
            JcrPropertyUtil.setProperty(this.slaNode, JcrServiceLevelAgreement.NAME, this.name);
            JcrPropertyUtil.setProperty(this.slaNode, JcrServiceLevelAgreement.DESCRIPTION, this.description);
            
            return new JcrServiceLevelAgreement(this.slaNode);
        }
    }

    private static class ObligationBuilderImpl<B> implements ObligationBuilder<B> {

        private Node obNode;
        
        private SLABuilderImpl slaBuilder;
        private ObligationGroupBuilderImpl groupBuilder;
        private String description;
        private Set<Metric> metrics = new HashSet<Metric>();

        public ObligationBuilderImpl(Node node, SLABuilderImpl bldr) {
            this.obNode = node;
            this.slaBuilder = bldr;
        }
        
        public ObligationBuilderImpl(Node node, ObligationGroupBuilderImpl bldr) {
            this.obNode = node;
            this.groupBuilder = bldr;
        }

        @Override
        public ObligationBuilder<B> description(String descr) {
            this.description = descr;
            return this;
        }

        @Override
        public ObligationBuilder<B> metric(Metric metric, Metric... more) {
            this.metrics.add(metric);
            for (Metric another : more) {
                this.metrics.add(another);
            }
            return this;
        }

        @Override
        public ObligationBuilder<B> metric(Collection<Metric> metrics) {
            this.metrics.addAll(metrics);
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public B build() {
            try {
                JcrPropertyUtil.setProperty(this.obNode, "jcr:description", this.description);

                for (Metric metric : this.metrics) {
                    Node metricNode = this.obNode.addNode(JcrObligation.METRICS, JcrObligation.METRIC_TYPE);
                    
                    JcrPropertyUtil.setProperty(metricNode, JcrObligation.NAME, metric.getClass().getSimpleName());
                    JcrPropertyUtil.setProperty(metricNode, JcrObligation.DESCRIPTION, metric.getDescription());
                    JcrUtil.addGenericJson(metricNode, JcrObligation.JSON, metric);
                }
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Failed to build the obligation", e);
            }

            if (this.groupBuilder != null) {
                return (B) this.groupBuilder;
            } else {
                return (B) this.slaBuilder;
            }
        }
    }

    private static class ObligationGroupBuilderImpl implements ObligationGroupBuilder {

        private Node groupNode;
        private Condition condition;
        private SLABuilderImpl slaBuilder;
        
        public ObligationGroupBuilderImpl(Node node, Condition cond, SLABuilderImpl slaBuilder) {
            this.groupNode = node;
            this.condition = cond;
            this.slaBuilder = slaBuilder;
        }

        @Override
        public ObligationGroupBuilder obligation(Obligation obligation) {
            // TODO Does not to work in the current JCR implementation.  Perhaps it should not be supported at all in the builder.
            throw new UnsupportedOperationException();
        }

        @Override
        public ObligationBuilder<ObligationGroupBuilder> obligationBuilder() {
            try {
                Node obNode = this.groupNode.addNode(JcrObligationGroup.OBLIGATIONS, JcrObligationGroup.OBLIGATION_TYPE);
                return new ObligationBuilderImpl<ObligationGroupBuilder>(obNode, this);
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Failed to create the obligation group builder", e);
            }
        }

        @Override
        public ServiceLevelAgreementBuilder build() {
            JcrPropertyUtil.setProperty(this.groupNode, JcrObligationGroup.CONDITION, this.condition);
            return this.slaBuilder;
        }
    }
}