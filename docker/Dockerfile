FROM docker.elastic.co/elasticsearch/elasticsearch:5.6.16

RUN printf 'cluster.name: "docker-cluster"\
\nnetwork.host: 0.0.0.0\n\
\ndiscovery.zen.minimum_master_nodes: 1\
\nxpack.security.enabled: false\
' > /usr/share/elasticsearch/config/elasticsearch.yml
