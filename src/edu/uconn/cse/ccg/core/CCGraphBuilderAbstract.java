/**
 * Copyright (c) 2014, 2015 Nhan Nguyen.
 *
 * This file is part of Cfinder.
 *
 * Cfinder is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Cfinder is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with Cfinder. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package edu.uconn.cse.ccg.core;

import edu.uconn.cse.ccg.model.ConfigurationOptionInformation;
import edu.uconn.cse.ccg.model.partI.PartICCGNode;
import edu.uconn.cse.ccg.model.partI.PartICCGraph;
import edu.uconn.cse.ccg.model.partII.PartIICCGNode;
import edu.uconn.cse.ccg.source.model.ClassHierarchy;
import edu.uconn.cse.ccg.source.model.ConditionalStmtUsage;
import edu.uconn.cse.ccg.source.model.MethodInfo;
import edu.uconn.cse.ccg.source.model.MethodLibraryUsage;
import edu.uconn.cse.ccg.source.model.MethodUsage;
import edu.uconn.cse.ccg.util.Utils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author nhannguyen
 *
 */
public abstract class CCGraphBuilderAbstract implements CCGraphBuilder {
  Set<String> javaLib = new HashSet<String>() {
    /**
     * 
     */
    private static final long serialVersionUID = 562450216661124249L;

    {
      add("java.lang.Class");
      add("java.lang.");
      add("java.io.");
      add("java.nio.");
      add("java.util.");
      add("java.net.");
      add("java.security.");
      add("javax.crypto.");
      add("java.math.");
      add("javax.servlet.");
      add("java.lang.Runnable");
    }
  };

  Set<String> logMethods = new HashSet<String>() {
    /**
     * 
     */
    private static final long serialVersionUID = 6656483381595903565L;

    {
      add("org.slf4j.Logger:info");
      add("org.slf4j.Logger:error");
    }
  };

  Set<String> convertType = new HashSet<String>() {
    /**
     * 
     */
    private static final long serialVersionUID = 7930628709938948187L;

    {
      add("java.lang.Integer:toString");
    }
  };

  Set<String> calledByCommandLine = new HashSet<String>() {
    /**
     * 
     */
    private static final long serialVersionUID = 8505517633045579851L;

    {
      add("org.apache.hadoop.hbase.regionserver.HRegionServer:HRegionServer");
      add("org.apache.hadoop.hbase.util.HBaseFsckRepair:fixMultiAssignment");
      add("org.apache.hadoop.hbase.util.HBaseFsck:HBaseFsck");
      add("org.apache.hadoop.hbase.io.hfile.HFilePrettyPrinter:parseOptions");
      add("org.apache.cassandra.tools.SSTableExport:export");
      add("org.apache.cassandra.tools.NodeProbe:takeSnapshot");
      add("org.apache.cassandra.cli.CliMain:processStatement");
      // added 07112015
      add("org.apache.cassandra.utils.MergeIterator.ManyToOne:computeNext");
    }
  };

  Set<String> calledByOtherProgram = new HashSet<String>() {
    /**
     * 
     */
    private static final long serialVersionUID = 8739532446826671878L;

    {
      add("org.apache.hadoop.hbase.LocalHBaseCluster:LocalHBaseCluster");
      add("org.apache.hadoop.hbase.master.AssignmentManager:process");
      // add("org.apache.hadoop.hbase.master.balancer.StochasticLoadBalancer:onConfigurationChange");
      // add("org.apache.hadoop.hbase.constraint.Constraints:setConfiguration");
      add("org.apache.hadoop.hbase.util.FSUtils:isAppendSupported");
      add("org.apache.hadoop.hbase.mapred.TableMapReduceUtil:initTableReduceJob");
      add("org.apache.hadoop.hbase.master.snapshot.SnapshotManager:SnapshotManager");
      add("org.apache.hadoop.hbase.backup.example.HFileArchiveManager:HFileArchiveManager"); // can
                                                                                             // be
                                                                                             // found
                                                                                             // in
      add("org.apache.hadoop.hbase.regionserver.RegionCoprocessorHost:handleCoprocessorThrowableNoRethrow:");
      add("org.apache.hadoop.hbase.util.BloomFilterFactory:getMaxKeys");
      add("org.apache.hadoop.hbase.mapreduce.TableInaddFormatBase:createRecordReader");
      add("org.apache.hadoop.hbase.util.FSUtils:getTotalTableFragmentation");
      add("org.apache.hadoop.hbase.client.TableSnapshotScanner:TableSnapshotScanner");
      add("org.apache.hadoop.hbase.security.access.ZKPermissionWatcher:nodeChildrenChanged");
      add("org.apache.hadoop.hbase.tmpl.master.AssignmentManagerStatusTmpl:renderTo");
      add("org.apache.hadoop.hbase.tmpl.master.MasterStatusTmpl:renderTo");
      add("org.apache.hadoop.hbase.procedure.ZKProcedureMemberRpcs:nodeCreated");
      add("org.apache.hadoop.hbase.mapreduce.addSortReducer:reduce"); // might be used by other
                                                                      // module
      add(" org.apache.hadoop.hbase.master.CatalogJanitor:chore");
      add("org.apache.hadoop.hbase.master.CatalogJanitor:initialChore");
      add("org.apache.hadoop.hbase.regionserver.compactions.CompactionPolicy:CompactionPolicy");
      add("org.apache.hadoop.hbase.ipc.FifoRpcScheduler:FifoRpcScheduler");
      add("org.apache.cassandra.thrift.CustomTThreadPoolServer:CustomTThreadPoolServer");
      add("org.apache.cassandra.db.SystemKeyspace:updateTokens");
      // added 06302015
       add("org.apache.cassandra.thrift.CassandraServer:get_count"); // removed 07152015
       add("org.apache.cassandra.thrift.CassandraServer:get"); // removed 07152015
       add("org.apache.cassandra.thrift.CassandraServer:multiget_count"); // removed 07152015
       add("org.apache.cassandra.thrift.CassandraServer:multiget_slice"); // removed 07152015
       add("org.apache.cassandra.thrift.CassandraServer:get_slice"); // removed 07152015
       add("org.apache.cassandra.thrift.CassandraServer:system_update_column_family");// removed 07152015
       add("org.apache.cassandra.thrift.CassandraServer:system_add_keyspace");// removed 07152015
      add("org.apache.cassandra.thrift.CassandraServer:system_add_column_family");// removed
                                                                                  // 07152015
       add("org.apache.cassandra.thrift.CassandraServer:login");// removed 07152015
      add("org.apache.cassandra.thrift.CassandraServer:execute_prepared_cql_query"); // removed
                                                                                     // 07152015
       add("org.apache.cassandra.thrift.CassandraServer:execute_cql_query"); // removed 07152015
       add("org.apache.cassandra.thrift.CassandraServer:system_update_keyspace");
       add("org.apache.cassandra.thrift.CassandraServer:get_paged_slice");
      add("org.apache.cassandra.thrift.CassandraServer:execute_prepared_cql_query");
      add("org.apache.cassandra.thrift.CustomTThreadPoolServer:buildTServer");
      // add("org.apache.cassandra.thrift.CassandraServer:describe_local_ring"); // removed 07152015
      // add("org.apache.cassandra.thrift.CassandraServer:describe_ring");
      // add("org.apache.cassandra.thrift.CassandraServer:describe_schema_versions"); //removed
      // 07152015
      // add("org.apache.cassandra.thrift.CassandraServer:get_range_slices"); // removed 07152015
      // add("org.apache.cassandra.thrift.CassandraServer:describe_cluster_name");
      // add("org.apache.cassandra.thrift.CassandraServer:execute_prepared_cql3_query");// removed
      // 07152015
      // add("org.apache.cassandra.thrift.CassandraServer:remove_counter");
      // add("org.apache.cassandra.thrift.CassandraServer:add");
      // add("org.apache.cassandra.thrift.CassandraServer:get_indexed_slices");
      // add("org.apache.cassandra.thrift.CassandraServer:insert");// removed 07152015
      // add("org.apache.cassandra.thrift.CassandraServer:cas");// removed 07152015
      // add("org.apache.cassandra.thrift.CassandraServer:batch_mutate");// removed 07152015
      // add("org.apache.cassandra.thrift.CassandraServer:atomic_batch_mutate");// removed 07152015
      // add("org.apache.cassandra.thrift.CassandraServer:remove");
      // add("org.apache.cassandra.thrift.CassandraServer:truncate"); //removed 07152015
      // add("org.apache.cassandra.thrift.CassandraServer:system_drop_column_family");
      // add("org.apache.cassandra.thrift.CassandraServer:describe_snitch");
      // add("org.apache.cassandra.thrift.CassandraServer:describe_splits");
      // add("org.apache.cassandra.thrift.CassandraServer:describe_partitioner");
      add("org.apache.cassandra.repair.StreamingRepairTask:onSuccess"); // added 07022015
      add("org.apache.cassandra.repair.StreamingRepairTask:onFailure");
      add("org.apache.cassandra.locator.YamlFileNetworkTopologySnitch:YamlFileNetworkTopologySnitch");
      // add("org.apache.cassandra.thrift.CassandraServer:prepare_cql3_query"); // added 07032015
      add("org.apache.cassandra.io.sstable.CQLSSTableWriter:close"); // added 07062015
      add("org.apache.cassandra.service.EmbeddedCassandraService:start"); // added 07072015
      // added 07102015
      add("org.apache.hadoop.util.NativeCodeLoader:getLoadNativeLibraries");
      add("org.apache.hadoop.fs.LocalDirAllocator:ifExists");// :java.lang.String
                                                             // org.apache.hadoop.conf.Configuration");
      add("org.apache.hadoop.http.HtmlQuoting:quoteOutputStream");
      // added 07112015
      add("org.apache.cassandra.thrift.Cassandra.Processor.execute_prepared_cql3_query:getResult");
      add("org.apache.cassandra.transport.Server:Server");
      // added 07142015
      add("org.apache.cassandra.transport.Message.Dispatcher:channelRead0");
      add("org.apache.cassandra.hadoop.cql3.CqlBulkRecordWriter:write");
      add("org.apache.cassandra.cql3.QueryProcessor:executeInternalWithPaging");
      add("org.apache.cassandra.tools.NodeProbe:truncate"); // TODO: need to check
      add("org.apache.cassandra.gms.Gossiper:usesVnodes");
      add("org.apache.cassandra.service.StorageProxy:getNativeTransportMaxConcurrentConnectionsPerIp");
      add("org.apache.cassandra.transport.RequestThreadPoolExecutor:isShuttingDown");
      add("org.apache.cassandra.hadoop.pig.CassandraStorage:putNext");
      add("org.apache.cassandra.transport.ConnectionLimitHandler:channelActive");
      add("org.apache.cassandra.transport.Server.Initializer");
      add("org.apache.cassandra.db.RangeSliceCommand:toIndexScanCommand");
      // add("org.apache.cassandra.service.StorageService:deliverHints"); //removed 07152015
      add("org.apache.cassandra.io.sstable.SSTableSimpleWriter:SSTableSimpleWriter");
      add("org.apache.cassandra.db.compaction.CompactionManager.CompactionExecutor:afterExecute");
      add("org.apache.cassandra.transport.RequestThreadPoolExecutor:afterExecute");
      add("org.apache.cassandra.concurrent.DebuggableThreadPoolExecutor:afterExecute");
      add("org.apache.cassandra.concurrent.DebuggableScheduledThreadPoolExecutor:afterExecute");
      add("org.apache.cassandra.db.compaction.ParallelCompactionIterable.Unwrapper:computeNext");
      add("org.apache.cassandra.io.sstable.KeyIterator:computeNext");
      add("org.apache.cassandra.db.columniterator.IndexedSliceReader:computeNext");
      add("org.apache.cassandra.locator.GossipingPropertyFileSnitch:GossipingPropertyFileSnitch");
      // 07152015
      add("org.apache.cassandra.db.marshal.CompositeType.Builder:buildForRelation");
      add("org.apache.cassandra.db.marshal.CompositeType.Builder:buildAsEndOfRange");
      add("org.apache.cassandra.cql.QueryProcessor:describeSchemaVersions");
    }
  };

  Set<String> calledByOtherMethodIncludingParameter = new HashSet<String>() {
    {
      // added 07142015
      add("org.apache.cassandra.transport.Message.Dispatcher:messageReceived:org.jboss.netty.channel.ChannelHandlerContext\torg.jboss.netty.channel.MessageEvent");
      add("org.apache.cassandra.db.commitlog.BatchCommitLogExecutorService:BatchCommitLogExecutorService:int");
      add("org.apache.cassandra.db.ColumnFamilyStore:rebuildSecondaryIndex:java.lang.String");
      add("org.apache.cassandra.thrift.CassandraServer:execute_cql3_query:java.nio.ByteBuffer\torg.apache.cassandra.thrift.Compression\torg.apache.cassandra.thrift.ConsistencyLevel");
      add("org.apache.cassandra.transport.Frame.Decoder:decode:org.jboss.netty.channel.ChannelHandlerContext\torg.jboss.netty.channel.Channel\torg.jboss.netty.buffer.ChannelBuffer");
      // unsure
      add("org.apache.cassandra.io.sstable.SSTableLoader:onFailure:java.lang.Throwable");
      // sure
      add("org.apache.cassandra.io.sstable.SSTableLoader:onSuccess:org.apache.cassandra.streaming.StreamState");
      // added 07152015
      add("org.apache.cassandra.service.MigrationManager:announceTypeUpdate:org.apache.cassandra.db.marshal.UserType");
      add("org.apache.cassandra.service.MigrationManager:announceNewType:org.apache.cassandra.db.marshal.UserType");
      add("org.apache.cassandra.Util:serializeForSSTable:org.apache.cassandra.db.ColumnFamily");
    }
  };

  Set<String> unsureStartingPoints = new HashSet<String>() {
    /**
     * 
     */
    private static final long serialVersionUID = -1329536752381923543L;

    { // added 07022015
      add("org.apache.cassandra.db.PagedRangeCommand.Serializer:serializedSize");
      add("org.apache.cassandra.streaming.StreamRequest.StreamRequestSerializer:serializedSize");
      add("org.apache.cassandra.repair.messages.ValidationComplete.ValidationCompleteSerializer:serializedSize");
      add("org.apache.cassandra.db.PagedRangeCommand.Serializer:serialize");
      add("org.apache.cassandra.streaming.StreamRequest.StreamRequestSerializer:serialize");
      add("org.apache.cassandra.repair.messages.ValidationComplete.ValidationCompleteSerializer:serialize");
      add("org.apache.cassandra.dht.IncludingExcludingBounds:IncludingExcludingBounds");
      add("org.apache.cassandra.dht.ExcludingBounds:ExcludingBounds");
      add("org.apache.cassandra.streaming.messages.IncomingFileMessage:deserialize");
      add("org.apache.cassandra.io.util.SequentialWriter:open");
      add("org.apache.cassandra.cache.AutoSavingCache.Writer:tempCacheFile");
      add("org.apache.cassandra.metrics.CompactionMetrics:CompactionMetrics");
      add("org.apache.cassandra.service.ReadCallback:ReadCallback");
      // need to update multilevel hierarchy
      add("org.apache.cassandra.hadoop.BulkRecordWriter:write");
      // added 07032015
      add("org.apache.cassandra.db.compaction.LeveledCompactionStrategy.LeveledScanner:computeNext");
      // added 07092015
      add("org.apache.hadoop.ipc.RPC:waitForProxy");
      add("org.apache.hadoop.util.GenericOptionsParser:GenericOptionsParser");
      add("org.apache.hadoop.fs.FileContext.Util:listFiles");
      // added 07102015
      add("org.apache.hadoop.io.SequenceFile.Sorter:sortAndIterate");
      add("org.apache.hadoop.security.authorize.ProxyUsers:authorize");
      add("org.apache.hadoop.fs.viewfs.ConfigUtil:getHomeDirValue");// :org.apache.hadoop.conf.Configuration");
      // added 07142015
      add("org.apache.cassandra.cql3.functions.TokenFct:create");
      add("org.apache.cassandra.cache.SerializingCache:SerializingCache");
      add("org.apache.cassandra.transport.RequestThreadPoolExecutor:execute");
      add("org.apache.cassandra.transport.RequestThreadPoolExecutor:shutdown");
      add("org.apache.cassandra.transport.RequestThreadPoolExecutor:isTerminated");
      add("org.apache.cassandra.transport.RequestThreadPoolExecutor:awaitTermination");
      add("org.apache.cassandra.transport.RequestThreadPoolExecutor:shutdownNow");
      add("org.apache.cassandra.transport.RequestThreadPoolExecutor:isShutdown");
    }
  };

  Set<String> calledByOtherModule = new HashSet<String>() {
    {// added 07102015
      add("org.apache.hadoop.fs.FileContext:getLocalFSFileContext");
      add("org.apache.hadoop.security.SecurityUtil:doAsCurrentUser");
      add("org.apache.hadoop.util.HostsFileReader:HostsFileReader");
      add("org.apache.hadoop.security.UserGroupInformation:getPrimaryGroupName");
      add("org.apache.hadoop.security.UserGroupInformation:checkTGTAndReloginFromKeytab");
      add("org.apache.hadoop.io.retry.RetryUtils:getDefaultRetryPolicy");
      add("org.apache.hadoop.ipc.Client:getTimeout");
      add("SUB org.apache.hadoop.io.nativeio.NativeIO:getOwner");// :java.io.FileDescriptor");
    }
  };

  Set<String> calledByMain = new HashSet<String>() {
    /**
     * 
     */
    private static final long serialVersionUID = 7763075660982817534L;

    {
      add("org.apache.cassandra.service.CassandraDaemon:init");
    }
  };

  Set<String> calledByMBean = new HashSet<String>() {
    /**
     * 
     */
    private static final long serialVersionUID = 2547790810944365516L;

    {
      add("org.apache.cassandra.net.MessagingService:apply");
      add("org.apache.cassandra.db.BatchlogManager:process");
      add("org.apache.cassandra.db.BatchlogManager:runMayThrow");
      add("org.apache.cassandra.db.compaction.CompactionManager:call");
      add("org.apache.cassandra.db.compaction.CompactionManager:disableAutoCompaction");
      add("org.apache.cassandra.gms.Gossiper:response");
      add("org.apache.cassandra.db.ColumnFamilyStore:startCompaction");
      add("org.apache.cassandra.service.StorageProxy:runMayThrow");
      add("org.apache.cassandra.service.StorageProxy:apply");
      add("org.apache.cassandra.db.commitlog.CommitLog:resetUnsafe");
      add("org.apache.cassandra.db.ColumnFamilyStore:search");
      add("org.apache.cassandra.db.ColumnFamilyStore:getRangeSlice");
      add("org.apache.cassandra.service.StorageService:apply");
      add("org.apache.cassandra.service.StorageProxy:cas"); // added 06302015
      add("org.apache.cassandra.service.StorageProxy:getLiveSortedEndpoints");
      add("org.apache.cassandra.service.StorageService:calculateToFromStreams");
      add("org.apache.cassandra.db.compaction.CompactionManager:doCleanupCompaction");
      add("org.apache.cassandra.db.HintedHandOffManager:HintedHandOffManager");
      add("org.apache.cassandra.service.StorageService:initClient");
      add("org.apache.cassandra.db.BatchlogManager:process");
      add("org.apache.cassandra.service.StorageProxy:performWrite");
      add("org.apache.cassandra.service.StorageService:setPartitionerUnsafe");
    }
  };

  Set<String> calledByThread = new HashSet<String>() {
    /**
     * 
     */
    private static final long serialVersionUID = -7359367226894220654L;

    {
      add("org.apache.hadoop.hbase.ipc.FifoRpcScheduler:FifoRpcScheduler");
      // add("org.apache.hadoop.hbase.util.HBaseFsckRepair:fixMultiAssignment");
    }
  };

  public Set<String> classToInterface = new HashSet<String>() {
    /**
     * 
     */
    private static final long serialVersionUID = -269122646874757188L;

    { // map an implemented class to its abstract or interface
      add("java.util.List:java.util.Collection");
      add("java.util.Collection:java.lang.Iterable");
      add("java.util.Set:java.lang.Iterable"); // added 07032015
      add("java.util.List:java.lang.Iterable");
      // need to remove then the tool will create this list
      add("org.apache.cassandra.dht.LocalPartitioner:org.apache.cassandra.dht.IPartitioner");
      add("org.apache.cassandra.cql3.statements.BatchStatement.PreparedBatchVariables:org.apache.cassandra.cql3.statements.BatchStatement.BatchVariables");
      add("org.apache.cassandra.cql3.statements.CQL3CasConditions:org.apache.cassandra.service.CASConditions");
      add("org.apache.cassandra.db.DecoratedKey:org.apache.cassandra.dht.RingPosition");
      add("org.apache.cassandra.dht.Token:org.apache.cassandra.dht.RingPosition");
      add("org.apache.cassandra.db.CounterMutation:org.apache.cassandra.db.IMutation");
      add("org.apache.cassandra.transport.Server.ConnectionTracker:org.apache.cassandra.transport.Connection.Tracker");
      add("org.apache.cassandra.db.marshal.AbstractType:java.util.Comparator");
      add("org.apache.cassandra.dht.RingPosition:T");
      // added 07062015
      add("org.apache.cassandra.db.compaction.SSTableSplitter.StatsCollector:org.apache.cassandra.db.compaction.CompactionManager.CompactionExecutorStatsCollector");
      // added 07102015
      // TODO: need to check since the following pairs are in the same class
      add("org.apache.hadoop.security.protocolPB.RefreshAuthorizationPolicyProtocolPB:java.lang.Object");
      add("org.apache.hadoop.ipc.TestRPCCompatibility.TestProtocol2:java.lang.Object");
      add("org.apache.hadoop.security.protocolPB.RefreshUserMappingsProtocolPB:java.lang.Object");
      add("org.apache.hadoop.tools.protocolPB.GetUserMappingsProtocolPB:java.lang.Object");
      // added 07142015
      add("java.net.InetAddress:R");
      add("org.apache.cassandra.db.OnDiskAtom:In");
      add("org.apache.cassandra.db.OnDiskAtom:Out");
      // added 07152015
      add("org.apache.cassandra.gms.GossipDigestAck2:T");
      add("org.apache.cassandra.db.RangeSliceCommand:T");
      add("org.apache.cassandra.gms.EchoMessage:T");
      add("org.apache.cassandra.db.TruncateResponse:T");
      add("org.apache.cassandra.db.RangeSliceReply:T");
      add("org.apache.cassandra.db.CounterMutation:T");
      add("java.util.UUID:T");
      add("org.apache.cassandra.db.ReadResponse:T");
      add("org.apache.cassandra.db.Mutation:T");
      add("org.apache.cassandra.db.Truncation:T");
      add("org.apache.cassandra.io.util.RandomAccessReader:org.apache.cassandra.io.util.FileDataInput");
    }
  };

  public Set<String> calledByTestFramework = new HashSet<String>() {
    {
      add("org.apache.cassandra.EmbeddedServer:startCassandra");
      add("org.apache.cassandra.io.sstable.IndexSummaryManager:IndexSummaryManager");
    }
  };

  public Set<String> depracatedMethod = new HashSet<String>();

  byte toBeContinued;
  boolean isInConditionalStatement = false;

  private int fcount = 0;
  private int countAssignment = 0;
  int gLevel = 0; // globalLevel
  int startLineOfContaintedMethod = -1;
  int endLineOfContainedMethod = -1;
  int countCalledByMain = 0;
  int countCalledByRun = 0;
  int countCalledByMXBean = 0;
  int countCalledByProtocolBuffer = 0;
  int countCalledByCommandLine = 0;
  int countCalledByServlet = 0;
  int countCalledByTestFramework = 0;
  int countCalledByOtherProgram = 0;
  int countCalledByMBean = 0;
  int countStartingPoint = 0;
  int nbOptionUsage = 0;
  boolean hasOracleFolder = false;

  String global_source_path;
  private String simpleClassName;
  private FileWriter tmpFileWriter;
  private FileWriter tempFileWriter;

  String oracleFolder;
  private String theConfigurationName;
  private String callerClassName; // TODO: need to check
  String partIICallerClassName;
  private String propReadFileName;
  private String correctContainedMethod = null;
  private CompilationUnit cuMethodUsageInfo;
  private CompilationUnit cuCreateReadFunctionFile;
  ICompilationUnit tempICompilationUnit;
  public static PartICCGraph graph = new PartICCGraph();
  private FileWriter partICCGWriter;
  private FileWriter partIICCGWriter;
  private FileWriter classHierarchytmpFileWriter;
  private String eclipseDirectoryPath;
  private SimpleName variableFieldName = null;
  private SimpleName fieldName = null;
  private QualifiedName qVariableFieldName = null;
  private FieldAccess fieldAccess = null;

  CompilationUnit cuDirectVariableCheck;
  ICompilationUnit savediCompilationUnit;

  int nbMethodInvoCalls = 0;
  int countField = 0;
  int countVariable = 0;
  final int NOTRECURSIVE = 0; // to indicate that the function is called by ubdateAnalyticalMap
  final int RECURSIVE = 0;
  boolean isModifiedGlobal;
  CompilationUnit cuAnalyzeMethodInvocation;
  String methodName;
  String methodSignature;
  String methodSig = null;

  Set<String> visitedMethods = new HashSet<String>();
  Map<String, Integer> smOptions = new HashMap<String, Integer>();
  Map<String, Integer> scOptions = new HashMap<String, Integer>();
  Map<String, Integer> spOptions = new HashMap<String, Integer>();
  Set<ASTNode> visitedAssignments = new HashSet<ASTNode>();
  Set<ASTNode> visitedParentNodes = new HashSet<ASTNode>();
  int[] typeOfParents = new int[100]; // to keep track types of all possible parents of a
                                      // configuration option. For example, methodinvocation, if,
                                      // ...
  HashMap<String, ClassHierarchy> classHierarchyModel = new HashMap<String, ClassHierarchy>();
  HashMap<String, ConfigurationOptionInformation> optionInfoMap =
      new HashMap<String, ConfigurationOptionInformation>();
  List<Integer> startLineofMethods = new ArrayList<Integer>();
  List<Integer> endLineofMethods = new ArrayList<Integer>();
  List<String> methodNames = new ArrayList<String>();
  Set<String> mxBeanClasses = new HashSet<String>();
  Set<String> reflectionClasses = new HashSet<String>();
  // private Map<String, String> returnStatementMethodLineMap = new LinkedHashMap<String, String>();
  private Map<String, String> innerClassMap = new LinkedHashMap<String, String>();
  private HashMap<String, MethodUsage> methodUsageContainer = new HashMap<String, MethodUsage>();
  HashMap<String, Set<String>> optionStartingPointMap = new HashMap<String, Set<String>>();
  private HashMap<String, Set<SuperInterface>> subClassInfo =
      new HashMap<String, Set<SuperInterface>>();
  private Set<String> visited = new HashSet<String>();
  Set<String> declaredName = new HashSet<String>();
  private List<ConditionalStmtUsage> conditionalStmtUsage = new ArrayList<ConditionalStmtUsage>();
  Set<String> realStartingPoints = new HashSet<String>();
  Set<ASTNode> visitedVariableFiled = new HashSet<ASTNode>();
  private HashSet<String> configNames = new HashSet<String>();
  HashMap<Integer, String> secondIndex = new HashMap<Integer, String>();
  private Set<String> visitedSimpleName = new HashSet<String>();
  Set<MethodLibraryUsage> methodLibUsage = new HashSet<MethodLibraryUsage>();
  IPackageFragment[] globalPackages;
  int countInvokedAbstractInterface = 0;
  Set<String> invokedAbstractInterfaces = new HashSet<String>();
  int countInvokedImplementationClasses = 0;
  Set<String> invokedImplementationClasses = new HashSet<String>();
  int countInvokedInsideAbstractInterface = 0;
  Set<String> invokedInsideAbstractInterface = new HashSet<String>();
  int countMethodInvocation = 0;

  protected class SuperInterface {
    public int isParameter = 0;
    public String parameterType;
    public String className;
    public Set<String> overrideMethods = new HashSet<String>();

    public SuperInterface(int isParameter, String parameterType, String className) {
      this.isParameter = isParameter;
      this.parameterType = parameterType;
      this.className = className;
    }

    public void addOverrideMethod(String methodName) {
      overrideMethods.add(methodName);
    }

  }

  public CCGraphBuilderAbstract(String propReadFileName, String eclipseDirectoryPath,
      String resultFileName) {
    this.propReadFileName = propReadFileName;
    this.eclipseDirectoryPath = eclipseDirectoryPath;
    try {
      Calendar cal = Calendar.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
      // System.out.println( sdf.format(cal.getTime()) );
      this.partICCGWriter =
          new FileWriter(eclipseDirectoryPath + "ccgresult/partICCG-" + resultFileName
              + sdf.format(cal.getTime()));
      this.partIICCGWriter =
          new FileWriter(eclipseDirectoryPath + "ccgresult/partIICCG-" + resultFileName);
      this.tmpFileWriter = new FileWriter(eclipseDirectoryPath + "ccgtemp/2-" + resultFileName);
      this.tempFileWriter = new FileWriter(eclipseDirectoryPath + "ccgtemp/temp" + resultFileName);
      this.classHierarchytmpFileWriter =
          new FileWriter(eclipseDirectoryPath + "ccgtemp/CH-" + resultFileName);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }


  void createReadFunctionFile(final IPackageFragment[] packages, final String source_path)
      throws JavaModelException {
    for (IPackageFragment mypackage : packages) {
      if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
        // System.err.println("Package " + mypackage.getElementName());

        for (ICompilationUnit iCompilationUnit : mypackage.getCompilationUnits()) {
          // System.out.println(iCompilationUnit.getPath().toString());
          if (iCompilationUnit.getPath().toString().contains(source_path)
              && iCompilationUnit.getElementName().equals(propReadFileName)) {

            ASTParser parser = ASTParser.newParser(AST.JLS8);
            parser.setSource(iCompilationUnit); // source from compilation unit
            parser.setResolveBindings(true);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            cuCreateReadFunctionFile = (CompilationUnit) parser.createAST(null);
            if (cuCreateReadFunctionFile != null && cuCreateReadFunctionFile.getPackage() != null
                && cuCreateReadFunctionFile.getPackage().getName() != null) {
              cuCreateReadFunctionFile.accept(new ASTVisitor() {
                // public boolean visit(MethodDeclaration node) {
                // if (!node.resolveBinding().getReturnType().getName().equals("void")
                // && node.parameters().size() > 0) {
                // System.out.println(node.getName().getFullyQualifiedName() + " : "
                // + node.resolveBinding().getReturnType().getName());
                // System.out.println(node.parameters().toString());
                // }
                // return true;
                // }

                @Override
                public boolean visit(VariableDeclarationFragment node) {
                  if (node.resolveBinding() != null) {
                    if (node.resolveBinding().isField()) {
                      if (node.getInitializer() != null)
                        // System.out.println(node.getName().getFullyQualifiedName() + " : " +
                        // node.getInitializer());
                        declaredName.add(node.getName().getFullyQualifiedName());
                    }
                  }
                  return true;
                }

                @Override
                public boolean visit(Assignment node) {
                  // System.out.println(node.getLeftHandSide() + " : " + node.getRightHandSide());
                  // System.out.println(node.getFullyQualifiedName());
                  // if (declaredName.contains(node.getFullyQualifiedName())) {
                  // System.out.println();
                  // if (node.getParent().getNodeType() == ASTNode.ASSIGNMENT) {
                  // Assignment assignment = (Assignment)node.getParent();
                  // System.out.println(node.getFullyQualifiedName() + " : " +
                  // assignment.getRightHandSide());
                  // } else if (node.getParent().getParent().getNodeType() == ASTNode.ASSIGNMENT) {
                  // Assignment assignment = (Assignment)node.getParent().getParent();
                  // System.out.println(node.getFullyQualifiedName() + " : " +
                  // assignment.getRightHandSide());
                  // } else if (node.getParent().getParent().getParent().getNodeType() ==
                  // ASTNode.ASSIGNMENT) {
                  // Assignment assignment = (Assignment)node.getParent().getParent().getParent();
                  // System.out.println(node.getFullyQualifiedName() + " : " +
                  // assignment.getRightHandSide());
                  // }
                  // }
                  return true;
                }

              });
            }
          }
        }
      }
    }
  }

  /**
   * 
   * @param fileName
   * @throws IOException temporiraly remove getValByRegex : Map<String,String>
   */
  public void readInputData(Map<String, String> inputMap, String fileName) throws IOException {
    BufferedReader br =
        new BufferedReader(new FileReader(eclipseDirectoryPath + "ccgdata/" + fileName));
    try {
      String line = br.readLine();

      while (line != null) {
        String[] option = line.split(":");
        if (option.length == 2) {
          inputMap.put(option[0].trim(), option[1].trim());
        } else {
          inputMap.put(option[0].trim(), "nil");
        }
        line = br.readLine();
      }
    } finally {
      br.close();
    }

    // for (String key : configReadFunctionMap.keySet()) {
    // System.err.println(key + " : " + configReadFunctionMap.get(key));
    // }
  }

  public ClassHierarchy getClassHierarchy(int type, boolean isInner, String className) {
    ClassHierarchy classHierarchy;
    if (classHierarchyModel.containsKey(className)) {
      classHierarchy = classHierarchyModel.get(className);
    } else {
      classHierarchy = new ClassHierarchy(type, isInner);
    }
    return classHierarchy;
  }

  String getMethodSignatureFromArguments(List<Expression> arguments) {
    StringBuilder result = new StringBuilder("");
    String paramType = null;
    if (arguments.size() == 1 && (arguments.get(0) instanceof ClassInstanceCreation)) {
      ClassInstanceCreation clIns = (ClassInstanceCreation) arguments.get(0);
      paramType = clIns.getType().resolveBinding().getQualifiedName();
      if (paramType.contains("<")) {
        paramType = paramType.substring(0, paramType.indexOf("<"));
      }
      // System.out.println("PARAM TYPE " + paramType);
      return paramType;
    }
    // System.out.println("NORMAL TYPE");
    if (arguments.size() > 0) {
      for (int i = 0; i < arguments.size() - 1; ++i) {
        Expression expr = arguments.get(i);
        if (expr instanceof ClassInstanceCreation) {
          ClassInstanceCreation clIns = (ClassInstanceCreation) expr;
          paramType = clIns.getType().resolveBinding().getQualifiedName();
          if (paramType.contains("<")) {
            paramType = paramType.substring(0, paramType.indexOf("<"));
          }
          // System.out.println("PARAM TYPE " + paramType);
          result.append(paramType).append("\t");
          continue;
        }
        if (expr.resolveTypeBinding() != null) {
          String exprStr = expr.resolveTypeBinding().getQualifiedName();
          StringBuilder exprStrBuilder = new StringBuilder();
          if (exprStr.contains("<")) {
            exprStrBuilder.append(exprStr.substring(0, exprStr.indexOf("<"))); // +1)).append(">");
            exprStr = exprStrBuilder.toString();
          }
          result.append(exprStr).append("\t");
        } else {
          result.append("nil\t");
        }
      }
      Expression expr = arguments.get(arguments.size() - 1);
      if (expr instanceof ClassInstanceCreation) {
        ClassInstanceCreation clIns = (ClassInstanceCreation) expr;
        paramType = clIns.getType().resolveBinding().getQualifiedName();
        if (paramType.contains("<")) {
          paramType = paramType.substring(0, paramType.indexOf("<"));
        }
        // System.out.println("PARAM TYPE " + paramType);
        result.append(paramType);
        return result.toString();
      }
      if (expr.resolveTypeBinding() != null) {
        String exprStr = expr.resolveTypeBinding().getQualifiedName();
        StringBuilder exprStrBuilder = new StringBuilder();
        if (exprStr.contains("<")) {
          exprStrBuilder.append(exprStr.substring(0, exprStr.indexOf("<"))); // +1)).append(">");
          exprStr = exprStrBuilder.toString();
        }
        result.append(exprStr);
      } else {
        result.append("nil");
      }
    }
    // for (Expression expr : arguments) {
    // // Utils.printResult(expr);
    // if (expr.resolveTypeBinding() != null) {
    // result.append(expr.resolveTypeBinding().getQualifiedName());
    // } else {
    // result.append("nil");
    // }
    // }
    return result.toString();
  }

  public static String getMethodSignatureFromParameters(List<SingleVariableDeclaration> parameters) {
    StringBuilder result = new StringBuilder("");
    if (parameters.size() > 0) {
      for (int i = 0; i < parameters.size() - 1; ++i) {
        SingleVariableDeclaration expr = parameters.get(i);
        if (expr.getType().resolveBinding() != null) {
          // String exprStr = expr.resolveBinding().getType().getQualifiedName(); //test
          String exprStr = expr.getType().resolveBinding().getQualifiedName();
          StringBuilder exprStrBuilder = new StringBuilder();
          if (exprStr.contains("<")) {
            exprStrBuilder.append(exprStr.substring(0, exprStr.indexOf("<"))); // +1)).append(">");
            exprStr = exprStrBuilder.toString();
          }
          if (expr.getExtraDimensions() == 1 && !exprStr.contains("[")) {
            exprStr = exprStr + "[]";
          }
          result.append(exprStr).append("\t");
          // result.append(expr.getType().resolveBinding().getQualifiedName()).append("\t");
        } else {
          result.append("nil\t");
        }
      }
      // System.out.println(parameters.get(parameters.size()-1));
      SingleVariableDeclaration expr = parameters.get(parameters.size() - 1);
      if (expr.getType().resolveBinding() != null) {
        // System.out.println("expr " + expr);
        String exprStr = expr.getType().resolveBinding().getQualifiedName();
        StringBuilder exprStrBuilder = new StringBuilder();
        if (exprStr.contains("<")) {
          exprStrBuilder.append(exprStr.substring(0, exprStr.indexOf("<"))); // +1)).append(">");
          exprStr = exprStrBuilder.toString();
        }
        // System.out.println("exprStr " + exprStr);
        if (expr.getExtraDimensions() == 1 && !exprStr.contains("[")) {
          exprStr = exprStr + "[]";
        }
        result.append(exprStr);
        // result.append(expr.getType().resolveBinding().getQualifiedName());
      } else {
        result.append("nil");
      }
    }

    // for (SingleVariableDeclaration param : parameters) {
    // if (param.getType().resolveBinding() != null)
    // result.append(param.getType().resolveBinding().getQualifiedName());
    // }

    return result.toString();
  }

  // test
  // public static String getMethodSignatureFromParameters2(List<SingleVariableDeclaration>
  // parameters) {
  // StringBuilder result = new StringBuilder("");
  // if (parameters.size() > 0) {
  // for (int i = 0; i < parameters.size() - 1; ++i) {
  // SingleVariableDeclaration expr = parameters.get(i);
  // if (expr.getType().resolveBinding() != null) {
  // // String exprStr = expr.resolveBinding().getType().getQualifiedName(); //test
  // String exprStr = expr.getType().resolveBinding().getQualifiedName();
  // StringBuilder exprStrBuilder = new StringBuilder();
  // if (exprStr.contains("<")) {
  // exprStrBuilder.append(exprStr.substring(0, exprStr.indexOf("<"))); // +1)).append(">");
  // exprStr = exprStrBuilder.toString();
  // }
  // if (expr.getExtraDimensions() == 1 && !exprStr.contains("[")) {
  // exprStr = exprStr + "[]";
  // }
  // result.append(exprStr).append("\t");
  // // result.append(expr.getType().resolveBinding().getQualifiedName()).append("\t");
  // } else {
  // result.append("nil\t");
  // }
  // }
  // // System.out.println(parameters.get(parameters.size()-1));
  // SingleVariableDeclaration expr = parameters.get(parameters.size() - 1);
  // if (expr.getType().resolveBinding() != null) {
  // System.out.println("expr " + expr);
  // String exprStr = expr.getType().resolveBinding().getQualifiedName();
  // StringBuilder exprStrBuilder = new StringBuilder();
  // if (exprStr.contains("<")) {
  // exprStrBuilder.append(exprStr.substring(0, exprStr.indexOf("<"))); // +1)).append(">");
  // exprStr = exprStrBuilder.toString();
  // }
  // if (expr.getExtraDimensions() == 1 && !exprStr.contains("[")) {
  // exprStr = exprStr + "[]";
  // }
  // System.out.println("exprStr " + exprStr + " " + expr.getExtraDimensions());
  // result.append(exprStr);
  // // result.append(expr.getType().resolveBinding().getQualifiedName());
  // } else {
  // result.append("nil");
  // }
  // }
  //
  // // for (SingleVariableDeclaration param : parameters) {
  // // if (param.getType().resolveBinding() != null)
  // // result.append(param.getType().resolveBinding().getQualifiedName());
  // // }
  //
  // return result.toString();
  // }


  boolean checkStandardLib(String className) {
    for (String lib : javaLib) {
      if (className.contains(lib)) {
        return false;
      }
    }
    return true;
  }

  public void addInheritanceInformation(Type t, int isParameterized, String concereteType,
      String interfaceClassName, String clName) {
    SuperInterface sInf = new SuperInterface(1, concereteType, interfaceClassName);
    Set<SuperInterface> sInfSet;
    if (subClassInfo.containsKey(clName)) {
      sInfSet = subClassInfo.get(clName);
    } else {
      sInfSet = new HashSet<SuperInterface>();
    }

    for (IMethodBinding tMethodBind : t.resolveBinding().getDeclaredMethods()) {
      // System.out.println("OverrideMethod  " + tMethodBind.getName());
      for (ITypeBinding tBind : tMethodBind.getTypeArguments()) {
        // System.out
        // .println(tBind.getQualifiedName());
      }
      sInf.addOverrideMethod(tMethodBind.getName());
      // tMethodBind.getMethodDeclaration().getMethodDeclara
      // System.out.println(tMethodBind.);
    }
    sInfSet.add(sInf);
    subClassInfo.put(clName, sInfSet);
  }

  public HashMap<String, Set<SuperInterface>> getInheritanceInformation(
      final IPackageFragment[] packages, final String source_path) throws JavaModelException {
    for (IPackageFragment mypackage : packages) {
      if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
        // System.err.println("Package " + mypackage.getElementName());

        for (ICompilationUnit iCompilationUnit : mypackage.getCompilationUnits()) {
          // System.out.println(iCompilationUnit.getPath().toString());

          if (iCompilationUnit.getPath().toString().contains(source_path)
              && !iCompilationUnit.getPath().toString().contains("thrift/gen-java")) {
            // && iCompilationUnit.getElementName().equals("CommitLog.java")) {


            ASTParser parser = ASTParser.newParser(AST.JLS8);
            parser.setSource(iCompilationUnit); // source from compilation unit
            parser.setResolveBindings(true);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);

            cuMethodUsageInfo = (CompilationUnit) parser.createAST(null);

            if (cuMethodUsageInfo != null && cuMethodUsageInfo.getPackage() != null
                && cuMethodUsageInfo.getPackage().getName() != null) {
              simpleClassName = iCompilationUnit.getElementName().replace(".java", "");
              callerClassName = mypackage.getElementName() + "." + simpleClassName;

              cuMethodUsageInfo.accept(new ASTVisitor() {

                @Override
                public boolean visit(TypeDeclaration node) {
                  // System.out.println("==TypeDeclaration==");
                  // System.out.println("implements interface " + node.superInterfaceTypes());
                  // System.out.println("extends class " + node.getSuperclassType());

                  String clName = callerClassName;
                  if (!node.getName().getFullyQualifiedName().equals(simpleClassName)) {
                    // System.out.println("TypeDeclaration " + callerClassName + " : " +
                    // node.getName().getFullyQualifiedName());
                    // clName = callerClassName + "." + node.getName().getFullyQualifiedName();
                    clName = node.resolveBinding().getQualifiedName();
                  }
                  // System.out.println("TypeDeclaration " + clName + " from "
                  // + cuMethodUsageInfo.getLineNumber(node.getStartPosition()) + " to " +
                  // cuMethodUsageInfo.getLineNumber(node.getStartPosition() + node.getLength()));


                  List list = node.superInterfaceTypes();


                  for (Object o : list) { // analyze super interface
                    Type t = (Type) o;
                    // System.out.println("Interface " + t.resolveBinding().getQualifiedName());
                    if (t instanceof ParameterizedType) {
                      ParameterizedType pType = (ParameterizedType) t;
                      if (pType.resolveBinding() != null) {
                        String classInfo = pType.resolveBinding().getQualifiedName();
                        String interfaceClassName = classInfo.substring(0, classInfo.indexOf("<"));
                        String concereteType =
                            classInfo.substring(classInfo.indexOf("<") + 1, classInfo.indexOf(">"));
                        // System.out.println("ParameterizedType " + interfaceClassName+ " TYPE " +
                        // concereteType
                        // );

                        // MethodUsage mUsage;
                        // if (parameterizedAbstractClass.containsKey(interfaceClassName)) {
                        // mUsage = parameterizedAbstractClass.get(interfaceClassName);
                        // } else {
                        // mUsage = new MethodUsage(interfaceClassName);
                        // }
                        // // parameterizedAbstractClass.put(classInfo.substring(0,
                        // classInfo.indexOf("<")), value)
                        //
                        // mUsage.addCallerMethod(concereteType, new MethodInfo(clName,
                        // callerClassName));
                        // parameterizedAbstractClass.put(interfaceClassName, mUsage);
                        // SuperInterface sInf = new SuperInterface(1, concereteType,
                        // interfaceClassName);
                        // Set<SuperInterface> sInfSet;
                        // if (subClassInfo.containsKey(clName)) {
                        // sInfSet = subClassInfo.get(clName);
                        // } else {
                        // sInfSet = new HashSet<SuperInterface>();
                        // }
                        //
                        // for (IMethodBinding tMethodBind :
                        // t.resolveBinding().getDeclaredMethods()) {
                        // // System.out.println("OverrideMethod  " + tMethodBind.getName());
                        // for (ITypeBinding tBind : tMethodBind.getTypeArguments()) {
                        // // System.out
                        // // .println(tBind.getQualifiedName());
                        // }
                        // sInf.addOverrideMethod(tMethodBind.getName());
                        // // tMethodBind.getMethodDeclaration().getMethodDeclara
                        // // System.out.println(tMethodBind.);
                        // }
                        // sInfSet.add(sInf);
                        // subClassInfo.put(clName, sInfSet);
                        addInheritanceInformation(t, 1, concereteType, interfaceClassName, clName);
                      }
                      // }
                    } else if (t instanceof QualifiedType) {
                      QualifiedType qType = (QualifiedType) t;
                      if (qType.resolveBinding() != null) {
                        // System.out.println("QualifiedType " +
                        // qType.getName().getFullyQualifiedName());
                        String classInfo = qType.resolveBinding().getQualifiedName();
                        // String interfaceClassName = classInfo.substring(0,
                        // classInfo.indexOf("<"));
                        // String concereteType = classInfo.substring(classInfo.indexOf("<") + 1,
                        // classInfo.indexOf(">"));
                        // System.out.println("ParameterizedType " + interfaceClassName+ " TYPE " +
                        // concereteType
                        // );

                        // SuperInterface sInf = new SuperInterface(0, "", classInfo);
                        // Set<SuperInterface> sInfSet;
                        // if (subClassInfo.containsKey(clName)) {
                        // sInfSet = subClassInfo.get(clName);
                        // } else {
                        // sInfSet = new HashSet<SuperInterface>();
                        // }
                        //
                        // for (IMethodBinding tMethodBind :
                        // t.resolveBinding().getDeclaredMethods()) {
                        // // System.out.println("OverrideMethod  " + tMethodBind.getName());
                        // for (ITypeBinding tBind : tMethodBind.getTypeArguments()) {
                        // // System.out
                        // // .println(tBind.getQualifiedName());
                        // }
                        // sInf.addOverrideMethod(tMethodBind.getName());
                        // // tMethodBind.getMethodDeclaration().getMethodDeclara
                        // // System.out.println(tMethodBind.);
                        // }
                        // sInfSet.add(sInf);
                        // subClassInfo.put(clName, sInfSet);
                        addInheritanceInformation(t, 0, "", classInfo, clName);
                      }
                    } else if (t instanceof SimpleType) {
                      SimpleType sType = (SimpleType) t;
                      if (sType.resolveBinding() != null) {
                        // System.out.println("SimpleType " +
                        // sType.getName().getFullyQualifiedName() + " :: "
                        // + sType.resolveBinding().getPackage().getName());
                        String classInfo = sType.resolveBinding().getQualifiedName();

                        // SuperInterface sInf = new SuperInterface(0, "", classInfo);
                        // Set<SuperInterface> sInfSet;
                        // if (subClassInfo.containsKey(clName)) {
                        // sInfSet = subClassInfo.get(clName);
                        // } else {
                        // sInfSet = new HashSet<SuperInterface>();
                        // }
                        //
                        // for (IMethodBinding tMethodBind :
                        // t.resolveBinding().getDeclaredMethods()) {
                        // // System.out.println("OverrideMethod  " + tMethodBind.getName());
                        // for (ITypeBinding tBind : tMethodBind.getTypeArguments()) {
                        // // System.out
                        // // .println(tBind.getQualifiedName());
                        // }
                        // sInf.addOverrideMethod(tMethodBind.getName());
                        // // tMethodBind.getMethodDeclaration().getMethodDeclara
                        // // System.out.println(tMethodBind.);
                        // }
                        // sInfSet.add(sInf);
                        // subClassInfo.put(clName, sInfSet);
                        addInheritanceInformation(t, 0, "", classInfo, clName);
                      }
                    } else {
                      // System.out.println("OtherType");
                    }
                  }


                  Type superClassType = node.getSuperclassType();
                  if (superClassType != null) { // analyze super class
                    // System.out.println("ExtendClass " +
                    // superClassType.resolveBinding().getQualifiedName());
                    Type t = superClassType;
                    if (t instanceof ParameterizedType) {
                      ParameterizedType pType = (ParameterizedType) t;
                      if (pType.resolveBinding() != null) {
                        String classInfo = pType.resolveBinding().getQualifiedName();
                        String interfaceClassName = classInfo.substring(0, classInfo.indexOf("<"));
                        String concereteType =
                            classInfo.substring(classInfo.indexOf("<") + 1, classInfo.indexOf(">"));
                        addInheritanceInformation(t, 1, concereteType, interfaceClassName, clName);
                      }
                    } else if (t instanceof QualifiedType) {
                      QualifiedType qType = (QualifiedType) t;
                      if (qType.resolveBinding() != null) {
                        // System.out.println("QualifiedType " +
                        // qType.getName().getFullyQualifiedName());
                        String classInfo = qType.resolveBinding().getQualifiedName();
                        addInheritanceInformation(t, 0, "", classInfo, clName);
                      }
                    } else if (t instanceof SimpleType) {
                      SimpleType sType = (SimpleType) t;
                      if (sType.resolveBinding() != null) {

                        // System.out.println("SimpleType " +
                        // sType.getName().getFullyQualifiedName() + " :: "
                        // + sType.resolveBinding().getPackage().getName());
                        String classInfo = sType.resolveBinding().getQualifiedName();

                        addInheritanceInformation(t, 0, "", classInfo, clName);
                      }
                    } else {
                      // System.out.println("OtherType");
                    }
                  }


                  // System.out.println("===");
                  return true;
                }

              });
            }

            // break;

          }
        }
      }

    }


    // for (String key : parameterizedAbstractClass.keySet()) {
    // MethodUsage m = parameterizedAbstractClass.get(key);
    // System.out.println("ATTENTION ");
    // System.out.println(m.className);
    // for (String key2 : m.methodUsageMap.keySet()) {
    // System.out.println(key2 + " => " );
    // Set<MethodInfo> tSet = m.methodUsageMap.get(key2);
    // for (MethodInfo mInfo : tSet) {
    // System.out.println(mInfo.methodSignature + " AND " + mInfo.className);
    // }
    // }
    // System.out.println("=====");
    // }

    // for (String key : subClassInfo.keySet()) {
    // System.out.println("ATTENTION");
    // System.out.println("METHOD " + key + " IS IMPLEMENTATION ");
    // Set<SuperInterface> sInfSet = subClassInfo.get(key);
    // for (SuperInterface sInf : sInfSet) {
    // System.out.println(" OF " + sInf.className + " WITH TYPE " + sInf.parameterType);
    // for (String mName : sInf.overrideMethods) {
    // System.out.println("OVERRIDE " + mName);
    // }
    // }
    //
    //
    // System.out.println("==========");
    // }

    return subClassInfo;
  }

  void updateMethodUsageInformation(String calleeClassName, String callerClassName,
      String calleeMethodName, String callerMethodName, int line) {
    MethodUsage methodUsage;
    if (methodUsageContainer.containsKey(calleeClassName)) {
      methodUsage = methodUsageContainer.get(calleeClassName);
    } else {
      methodUsage = new MethodUsage(calleeClassName);
    }
    // the usage of addCallerMethod here is not good
    // there are many instances of the same MethodInfo for a method
    // List<Expression> arguments = node.arguments();
    methodUsage.addCallerMethod(calleeMethodName, new MethodInfo(callerMethodName, callerClassName,
        line, cPath));
    // System.out.println("PUTTING " +
    // calleeMethodName+":"+getMethodSignature(arguments) + " INTO " +
    // calleeClassName);
    methodUsageContainer.put(calleeClassName, methodUsage);
  }

  /**
   * 
   * @param packages
   * @param global_source_path
   * @return
   * @throws JavaModelException
   * @throws IOException
   */
  int nbClassNameEmpty = 0;
  int nbMethodNameEmpty = 0;
  int nbClassInstanceCreation = 0;
  // map binding of a field to the corresponding method
  private Map<IBinding, MethodInfo> fieldMethodMap = new HashMap<IBinding, MethodInfo>();

  String cPath;

  // IVariableBinding[] fields;
  // Set<IVariableBinding> fieldSet;

  public HashMap<String, MethodUsage> getMethodUsageInformation(final IPackageFragment[] packages,
      final String source_path) throws JavaModelException, IOException {
    for (IPackageFragment mypackage : packages) {
      if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
        // System.err.println("Package " + mypackage.getElementName());

        for (ICompilationUnit iCompilationUnit : mypackage.getCompilationUnits()) {
          // System.out.println(iCompilationUnit.getPath().toString());

          if (iCompilationUnit.getPath().toString().contains(source_path)
              && !iCompilationUnit.getPath().toString().contains("thrift/gen-java")) {
            // && iCompilationUnit.getElementName().equals("QueryPagers.java")) {
            // {//AutoSavingCache.java")) {
            // //DefaultWALProvider.java")) {

            cPath = iCompilationUnit.getPath().toString();
            tempICompilationUnit = iCompilationUnit;

            ASTParser parser = ASTParser.newParser(AST.JLS8);
            parser.setSource(iCompilationUnit); // source from compilation unit
            parser.setResolveBindings(true);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            // returnStatementMethodLineMap.clear();
            innerClassMap.clear();
            startLineofMethods.clear();
            endLineofMethods.clear();
            methodNames.clear();
            startLineofMethods.add(-1);
            endLineofMethods.add(Integer.MAX_VALUE);
            methodNames.add(iCompilationUnit.getElementName().replace(".java", ""));
            cuMethodUsageInfo = (CompilationUnit) parser.createAST(null);

            if (cuMethodUsageInfo != null && cuMethodUsageInfo.getPackage() != null
                && cuMethodUsageInfo.getPackage().getName() != null) {
              simpleClassName = iCompilationUnit.getElementName().replace(".java", "");
              callerClassName = mypackage.getElementName() + "." + simpleClassName;
              if (callerClassName.contains("MXBean")) {
                mxBeanClasses.add(callerClassName);
              }
              // fields = null;
              // fieldSet = null;
              fieldMethodMap.clear();
              cuMethodUsageInfo.accept(new ASTVisitor() {

                @Override
                public boolean visit(TypeDeclaration node) {
                  // fields = node.resolveBinding().getDeclaredFields();
                  // fieldSet = new HashSet<IVariableBinding>(Arrays.asList(fields));
                  // for (IVariableBinding ivBind : fieldSet) {
                  // System.err.println("FIELD " + ivBind.getName());
                  // }

                  if (!node.getName().getFullyQualifiedName().equals(simpleClassName)) {
                    // System.out.println("TypeDeclaration " + callerClassName + " : " +
                    // node.getName().getFullyQualifiedName());
                    String clName = node.resolveBinding().getQualifiedName();
                    // }
                    // System.err.println("TypeDeclaration "
                    // + clName
                    // + " from "
                    // + cuMethodUsageInfo.getLineNumber(node.getStartPosition())
                    // + " to "
                    // + cuMethodUsageInfo.getLineNumber(node.getStartPosition()
                    // + node.getLength()));

                    innerClassMap.put(
                        cuMethodUsageInfo.getLineNumber(node.getStartPosition())
                            + ":"
                            + cuMethodUsageInfo.getLineNumber(node.getStartPosition()
                                + node.getLength()), clName);
                  }
                  return true;
                }

                @Override
                public boolean visit(MethodDeclaration node) {
                  if (node.resolveBinding().isDeprecated()) {
                    int line = cuMethodUsageInfo.getLineNumber(node.getStartPosition());
                    // System.err.println("ISDeprecated " + node.getName().getFullyQualifiedName());
                    String tCallerClassName = callerClassName;
                    int sLine = -1;
                    int eLine = Integer.MAX_VALUE;
                    for (String classNameKey : innerClassMap.keySet()) {
                      if (Integer.parseInt(classNameKey.split(":")[0]) <= line
                          && Integer.parseInt(classNameKey.split(":")[1]) >= line) {
                        if (Integer.parseInt(classNameKey.split(":")[0]) > sLine
                            && Integer.parseInt(classNameKey.split(":")[1]) < eLine) {
                          sLine = Integer.parseInt(classNameKey.split(":")[0]);
                          eLine = Integer.parseInt(classNameKey.split(":")[1]);
                          tCallerClassName = innerClassMap.get(classNameKey);
                          // System.out.println("inner " + node.getName().getFullyQualifiedName()
                          // +":" + getMethodSignature(node.arguments()) + " Found in " +
                          // callerMethodName + " " + tCallerClassName);
                        }
                      }
                    }
                    depracatedMethod.add(tCallerClassName + ":"
                        + node.getName().getFullyQualifiedName() + ":"
                        + getMethodSignatureFromParameters(node.parameters()));
                  }
                  startLineofMethods.add(cuMethodUsageInfo.getLineNumber(node.getStartPosition()));
                  endLineofMethods.add(cuMethodUsageInfo.getLineNumber(node.getStartPosition()
                      + node.getLength()));
                  methodNames.add(node.getName().getFullyQualifiedName() + ":"
                      + getMethodSignatureFromParameters(node.parameters()));
                  // System.out.println("HERE");
                  // System.out.println("INFO " + node.getName().getFullyQualifiedName() + ":"
                  // + getMethodSignatureFromParameters(node.parameters()));
                  return true;
                }

                // methodinvocation is used to create information of involked method
                @Override
                public boolean visit(MethodInvocation node) {
                  if (node.resolveMethodBinding() != null
                      && node.resolveMethodBinding().getDeclaringClass() != null) {
                    int line = cuMethodUsageInfo.getLineNumber(node.getStartPosition());
                    String calleeClassName =
                        node.resolveMethodBinding().getDeclaringClass().getQualifiedName();
                    Expression nodeExpr = node.getExpression();
                    if (nodeExpr != null) {
                      if (nodeExpr instanceof Name) {
                        // System.out.println("NAME");
                        Name name = (Name) nodeExpr;

                        // System.out.println(calleeClassName + " : "
                        // + name.resolveTypeBinding().getQualifiedName() + " :: "
                        // + node.getName().getFullyQualifiedName());
                        calleeClassName = name.resolveTypeBinding().getQualifiedName();

                      } else if (nodeExpr instanceof MethodInvocation) {
                        try {
                          tempFileWriter.write(calleeClassName + " : " + node.getParent() + ":"
                              + node.getName().getFullyQualifiedName() + " :: " + nodeExpr + "\n\n");
                        } catch (IOException e) {
                          // TODO Auto-generated catch block
                          e.printStackTrace();
                        }
                      } else {

                      }
                    }
                    // System.out.println("calleeClassName " + calleeClassName + " at " + line);
                    // if (classHierarchyModel.containsKey(calleeClassName)) {
                    // if (classHierarchyModel.get(calleeClassName).isAbstractClass() ||
                    // classHierarchyModel.get(calleeClassName).isInterface()) {
                    // // System.err
                    // // .println("INVOKEDABSTRACTINTERFACE");
                    // countInvokedAbstractInterface++;
                    // invokedAbstractInterfaces.add(calleeClassName);
                    // }
                    // if (classHierarchyModel.get(calleeClassName).isImplementationClass()) {
                    // // System.err
                    // // .println("INVOKEDABSTRACTINTERFACE");
                    // countInvokedImplementationClasses++;
                    // invokedImplementationClasses.add(calleeClassName);
                    // }
                    // }
                    // if (node.resolveMethodBinding().getDeclaringClass().isGenericType()) {
                    // System.out.println("GENERIC " + calleeClassName);
                    // }

                    String originalCalleeClassName = calleeClassName;
                    // if (node.resolveMethodBinding().getDeclaringClass().isParameterizedType()
                    if (calleeClassName.contains("<")) {
                      StringBuilder strBuilder = new StringBuilder();
                      calleeClassName =
                          strBuilder
                              .append(calleeClassName.substring(0, calleeClassName.indexOf("<")))
                              .append(
                                  calleeClassName.substring(calleeClassName.lastIndexOf(">") + 1,
                                      calleeClassName.length())).toString();
                      // System.out.println("PARAM " + calleeClassName + " " +
                      // node.resolveMethodBinding().getDeclaringClass().getName());
                    }

                    // if (node.resolveMethodBinding().getDeclaringClass()) {
                    // System.out.println("GENERIC " + calleeClassName);
                    // }
                    // if
                    // (node.resolveMethodBinding().getDeclaringClass().getQualifiedName().contains("org.apache.hadoop.ipc.FairCallQueue"))
                    // {
                    // System.out.println("CONTAIN " + calleeClassName);
                    // }
                    // if (checkStandardLib(calleeClassName)) { //TODO: temporarily disable
                    String calleeMethodName = node.getName().getFullyQualifiedName();
                    // if (calleeMethodName.equals("getDefaultCompressionType")) {
                    if (calleeClassName.equals("")) {
                      calleeClassName = callerClassName;
                      // System.out.println("FOUNDFOUND " + calleeClassName + " FROM " +
                      // callerClassName + " AT "
                      // + cuMethodUsageInfo.getLineNumber(node.getStartPosition()) + " METHOD " +
                      // calleeMethodName);

                      // nbClassNameEmpty++;
                    }

                    // if (calleeClassName.equals("")) {
                    // nbClassNameEmpty++;
                    // }
                    //
                    // if (calleeMethodName.equals("")) {
                    // nbMethodNameEmpty++;
                    // }

                    countMethodInvocation++;
                    VariableDeclarationFragment varDecFrag = null;
                    // we might need to add server upper level to cover all cases
                    if (node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
                      varDecFrag = (VariableDeclarationFragment) node.getParent();
                    } else if (node.getParent().getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
                      varDecFrag = (VariableDeclarationFragment) node.getParent().getParent();
                    } else if (node.getParent().getParent().getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
                      varDecFrag =
                          (VariableDeclarationFragment) node.getParent().getParent().getParent();
                    }
                    if (varDecFrag != null && varDecFrag.resolveBinding().isField()) {
                      fieldMethodMap.put(varDecFrag.getName().resolveBinding(), new MethodInfo(
                          calleeMethodName + ":"
                              + getMethodSignatureFromArguments(node.arguments()), calleeClassName));
                      // System.err.println("VARDEC " +
                      // varDecFrag.getName().getFullyQualifiedName());

                    } else {


                      // if (classHierarchyModel.containsKey(callerClassName))
                      // // if (classHierarchyModel.get(callerClassName).isAbstractClass() ||
                      // classHierarchyModel.get(callerClassName).isInterface())
                      // // invokedInsideAbstractInterface.add(callerClassName);
                      // // if (classHierarchyModel.get(callerClassName).isImplementationClass()) {
                      // //
                      // // }
                      // if (classHierarchyModel.get(callerClassName).isInterface()) {
                      // //|| classHierarchyModel.get(callerClassName).isInterface()) {
                      // countInvokedInsideAbstractInterface++;
                      // invokedInsideAbstractInterface.add(callerClassName);
                      // }


                      int statementType = -1; // type of statement which relate to this
                                              // methodinvocation
                      ASTNode directVariable = null; // the variable which is assginment by this
                                                     // method invocation

                      // System.out.print("MethodInvocation " +
                      // node.getName().getFullyQualifiedName()
                      // + " of "
                      // + calleeClassName
                      // + " has signature ");

                      // methodSignatureBuilder.append(calleeMethodName);
                      // List<Expression> args = node.arguments();
                      // List<Type> typeArgs = node.typeArguments();

                      // if (args.size() > 0) {
                      // // System.out
                      // // .println("has " + args.size() + "  argument(s) with " + typeArgs.size()
                      // +
                      // " type");
                      // for (Expression expr : args) {
                      // if (expr.resolveTypeBinding() != null) {
                      // // System.out
                      // // .println("Type " + expr.resolveTypeBinding().getName());
                      // methodSignatureBuilder.append(":").append(expr.resolveTypeBinding().getName());
                      // //Utils.getExpressionType(expr);
                      // }
                      // }
                      // } else {
                      // // System.out
                      // // .println("no arguments ");
                      // }


                      String callerMethodName;
                      // for (String methodNameKey : returnStatementMethodLineMap.keySet()) {
                      // if (Integer.parseInt(methodNameKey.split(":")[0]) <= line
                      // && Integer.parseInt(methodNameKey.split(":")[1]) >= line) {
                      // callerMethodName = returnStatementMethodLineMap.get(methodNameKey);
                      // }
                      // }
                      callerMethodName = findMethodName(line);
                      // System.out.println("callerMethodName : " + callerMethodName);


                      // Get the innter class (if any)
                      String tCallerClassName = callerClassName;
                      int sLine = -1;
                      int eLine = Integer.MAX_VALUE;
                      for (String classNameKey : innerClassMap.keySet()) {
                        if (Integer.parseInt(classNameKey.split(":")[0]) <= line
                            && Integer.parseInt(classNameKey.split(":")[1]) >= line) {
                          if (Integer.parseInt(classNameKey.split(":")[0]) > sLine
                              && Integer.parseInt(classNameKey.split(":")[1]) < eLine) {
                            sLine = Integer.parseInt(classNameKey.split(":")[0]);
                            eLine = Integer.parseInt(classNameKey.split(":")[1]);
                            tCallerClassName = innerClassMap.get(classNameKey);
                            // System.out.println("inner " + node.getName().getFullyQualifiedName()
                            // +":" + getMethodSignature(node.arguments()) + " Found in " +
                            // callerMethodName + " " + tCallerClassName);
                          }
                        }
                      }

                      // System.out.print(methodSignatureBuilder.toString());
                      // System.err.println(node.getName().getFullyQualifiedName()
                      // + node.resolveMethodBinding().getDeclaringClass().getQualifiedName() +
                      // " : "
                      // + node.getParent().getNodeType());
                      //

                      // System.out.print("METHODINVO " + calleeMethodName
                      // + getMethodSignatureFromArguments(node.arguments()) + " OF "
                      // + calleeClassName + " AT " + line);
                      // System.out.println(" of " + callerMethodName + " in " + tCallerClassName);

                      // System.out.println(node.arguments() + " : "
                      // + getMethodSignature(node.arguments()));
                      List<Expression> arguments = node.arguments();
                      updateMethodUsageInformation(calleeClassName, tCallerClassName,
                          calleeMethodName + ":"
                              + getMethodSignatureFromArguments(node.arguments()),
                          callerMethodName, line);
                      // MethodUsage methodUsage;
                      // if (methodUsageContainer.containsKey(calleeClassName)) {
                      // methodUsage = methodUsageContainer.get(calleeClassName);
                      // } else {
                      // methodUsage = new MethodUsage(calleeClassName);
                      // }
                      // // the usage of addCallerMethod here is not good
                      // // there are many instances of the same MethodInfo for a method
                      // List<Expression> arguments = node.arguments();
                      // methodUsage.addCallerMethod(calleeMethodName + ":"
                      // + getMethodSignatureFromArguments(arguments), new MethodInfo(
                      // callerMethodName, tCallerClassName, statementType, directVariable, line,
                      // cPath));
                      // // System.err.println("ADDING " + calleeMethodName + ":"
                      // // + getMethodSignature(arguments) + " FROM " + calleeClassName
                      // // + " CALLED BY " + callerMethodName + " OF " + tCallerClassName);
                      // methodUsageContainer.put(calleeClassName, methodUsage);

                      updateMethodUsageInformation(originalCalleeClassName, tCallerClassName,
                          calleeMethodName + ":" + getMethodSignatureFromArguments(arguments),
                          callerMethodName, line);
                      // if (methodUsageContainer.containsKey(originalCalleeClassName)) {
                      // methodUsage = methodUsageContainer.get(originalCalleeClassName);
                      // } else {
                      // methodUsage = new MethodUsage(originalCalleeClassName);
                      // }
                      // // the usage of addCallerMethod here is not good
                      // // there are many instances of the same MethodInfo for a method
                      // methodUsage.addCallerMethod(calleeMethodName + ":"
                      // + getMethodSignatureFromArguments(arguments), new MethodInfo(
                      // callerMethodName, tCallerClassName, statementType, directVariable, line,
                      // cPath));
                      // // System.err.println("ADDING " + calleeMethodName + ":"
                      // // + getMethodSignature(arguments) + " FROM " + calleeClassName
                      // // + " CALLED BY " + callerMethodName + " OF " + tCallerClassName);
                      // methodUsageContainer.put(originalCalleeClassName, methodUsage);

                      // Reflection Analysis
                      if (node.getName().getFullyQualifiedName().equals("getClass")) {
                        // System.out.println("Need to check Reflection in " + callerMethodName +
                        // " of "
                        // + tCallerClassName);

                        for (Expression param : arguments) {
                          if (param instanceof TypeLiteral) {
                            TypeLiteral reflectedClass = (TypeLiteral) param;

                            String qReflectionClassName =
                                reflectedClass.resolveTypeBinding().getQualifiedName();
                            String correctQReflectionClassName =
                                qReflectionClassName.substring(
                                    qReflectionClassName.indexOf("<") + 1,
                                    qReflectionClassName.indexOf(">"));
                            // simple name
                            String sReflectionClassName =
                                reflectedClass.resolveTypeBinding().getName();
                            String correctSReflectionClassName =
                                sReflectionClassName.substring(
                                    sReflectionClassName.indexOf("<") + 1,
                                    sReflectionClassName.indexOf(">"));
                            // System.out
                            // .println("Reflected " + correctQReflectionClassName + " " +
                            // correctSReflectionClassName
                            // + " at " + cuMethodUsageInfo.getLineNumber(node.getStartPosition())
                            // + " of " + tempICompilationUnit.getElementName());
                            updateMethodUsageInformation(correctQReflectionClassName,
                                tCallerClassName, correctSReflectionClassName, callerMethodName,
                                line);

                            // if (methodUsageContainer.containsKey(correctQReflectionClassName)) {
                            // methodUsage = methodUsageContainer.get(correctQReflectionClassName);
                            // } else {
                            // methodUsage = new MethodUsage(correctQReflectionClassName);
                            // }
                            // methodUsage.addCallerMethod(correctSReflectionClassName,
                            // new MethodInfo(callerMethodName, tCallerClassName, statementType,
                            // directVariable, line, cPath));
                            // methodUsageContainer.put(correctQReflectionClassName, methodUsage);
                            reflectionClasses.add(correctQReflectionClassName);
                          }
                        }
                        // }
                      }

                    }
                  }
                  return true;
                }

                @Override
                public boolean visit(ReturnStatement node) {
                  Expression expr = node.getExpression();
                  int line = cuMethodUsageInfo.getLineNumber(node.getStartPosition());
                  if (expr instanceof ClassInstanceCreation) {
                    ClassInstanceCreation cInstanceCreation = (ClassInstanceCreation) expr;
                    if (cInstanceCreation.resolveTypeBinding() != null) {

                      // System.out.println("ClassInstanceCreation " +
                      // cInstanceCreation.resolveConstructorBinding().getName() + " OF " +
                      // cInstanceCreation.resolveTypeBinding().getQualifiedNam

                      String calleeClassName =
                          cInstanceCreation.getType().resolveBinding().getQualifiedName(); // cInstanceCreation.getAnonymousClassDeclaration().resolveBinding().getQualifiedName();
                                                                                           // //cInstanceCreation.resolveTypeBinding().getQualifiedName();
                      String calleeMethodName = cInstanceCreation.resolveTypeBinding().getName(); // cInstanceCreation.resolveConstructorBinding().getName();

                      // if (calleeClassName.equals("")) {
                      // // calleeMethodName = calleeClassName;
                      // nbClassNameEmpty++;
                      // // System.out.println("FOUNDFOUND " + calleeClassName + " FROM " +
                      // callerClassName + " AT "
                      // // + cuMethodUsageInfo.getLineNumber(node.getStartPosition()) + " METHOD "
                      // + calleeMethodName);
                      // }
                      if (calleeMethodName.equals("")) {
                        calleeMethodName = calleeClassName;
                        // nbClassNameEmpty++;
                        // System.out.println("FOUNDFOUND " + calleeClassName + " FROM " +
                        // callerClassName + " AT "
                        // + cuMethodUsageInfo.getLineNumber(node.getStartPosition()) + " METHOD " +
                        // calleeMethodName);
                      }

                      if (calleeMethodName.equals("")) {
                        nbMethodNameEmpty++;
                      }

                      String callerMethodName;
                      // for (String methodNameKey : returnStatementMethodLineMap.keySet()) {
                      // if (Integer.parseInt(methodNameKey.split(":")[0]) <= line
                      // && Integer.parseInt(methodNameKey.split(":")[1]) >= line) {
                      // callerMethodName = returnStatementMethodLineMap.get(methodNameKey);
                      // }
                      // }
                      callerMethodName = findMethodName(line);

                      // Get the innter class (if any)
                      String tCallerClassName = callerClassName;
                      int sLine = -1;
                      int eLine = Integer.MAX_VALUE;
                      for (String classNameKey : innerClassMap.keySet()) {
                        if (Integer.parseInt(classNameKey.split(":")[0]) <= line
                            && Integer.parseInt(classNameKey.split(":")[1]) >= line) {
                          if (Integer.parseInt(classNameKey.split(":")[0]) > sLine
                              && Integer.parseInt(classNameKey.split(":")[1]) < eLine) {
                            sLine = Integer.parseInt(classNameKey.split(":")[0]);
                            eLine = Integer.parseInt(classNameKey.split(":")[1]);
                            tCallerClassName = innerClassMap.get(classNameKey);
                            // System.out.println("inner " + callerMethodName + " " +
                            // tCallerClassName);
                          }
                        }
                      }

                      // System.out.print(methodSignatureBuilder.toString());
                      // System.out.println(" of " + callerMethodName + " in " + tCallerClassName);
                      List<Expression> arguments = cInstanceCreation.arguments();
                      updateMethodUsageInformation(calleeClassName, tCallerClassName,
                          calleeMethodName + ":" + getMethodSignatureFromArguments(arguments),
                          callerMethodName, line);

                      // MethodUsage methodUsage;
                      // if (methodUsageContainer.containsKey(calleeClassName)) {
                      // methodUsage = methodUsageContainer.get(calleeClassName);
                      // } else {
                      // methodUsage = new MethodUsage(calleeClassName);
                      // }
                      // // the usage of addCallerMethod here is not good
                      // // there are many instances of the same MethodInfo for a method
                      //
                      // methodUsage.addCallerMethod(calleeMethodName + ":"
                      // + getMethodSignatureFromArguments(arguments), new MethodInfo(
                      // callerMethodName, tCallerClassName, line, cPath));
                      // methodUsageContainer.put(calleeClassName, methodUsage);
                    }
                  }
                  // else {
                  // System.err.println("Return Expr " + expr + " type "
                  // + Utils.getASTNodeType(expr)
                  // + " at " + cuMethodUsageInfo.getLineNumber(node.getStartPosition()));
                  // }
                  return true;
                }

                @Override
                public boolean visit(ClassInstanceCreation node) {
                  //
                  if (node.resolveTypeBinding() != null) {

                    String calleeClassName = node.resolveTypeBinding().getQualifiedName();
                    // if (calleeClassName.equals("")) {
                    // System.out.println(node.resolveConstructorBinding().getDeclaringClass()
                    // .getQualifiedName());
                    // System.out.println(node.getAnonymousClassDeclaration().resolveBinding()
                    // .getName());
                    // calleeClassName = node.resolveConstructorBinding().getName();
                    // System.out.println("Expression " + node.getExpression());
                    // }
                    // System.out.println(node.resolveConstructorBinding().getName());
                    // System.out.println("ClassInstanceCreation : " + calleeClassName + " AT "
                    // + cuMethodUsageInfo.getLineNumber(node.getStartPosition()));


                    // consider the case where an public static instance of a class is created
                    // if (calleeClassName.equals(callerClassName)) {
                    // if (node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT)
                    // {
                    // VariableDeclarationFragment vdFrag =
                    // (VariableDeclarationFragment) node.getParent();
                    // int modifierFlag = vdFrag.resolveBinding().getModifiers();
                    // if (Modifier.isFinal(modifierFlag) && Modifier.isStatic(modifierFlag)
                    // && Modifier.isPublic(modifierFlag)) {
                    // System.err.println("PUBLIC STATIC FINAL");
                    // }
                    // System.err.println("ASSIGNMENT " + callerClassName + " : "
                    // + node.getParent().getNodeType() + " TO "
                    // + vdFrag.getName().getFullyQualifiedName() + " with modifier "
                    // + vdFrag.resolveBinding().getModifiers()
                    // + " at " + cuMethodUsageInfo.getLineNumber(node.getStartPosition()));
                    // }
                    // }
                    String calleeMethodName = node.resolveTypeBinding().getName();// node.resolveConstructorBinding().getName();
                    if (calleeClassName.equals("")) {
                      calleeClassName = node.getType().resolveBinding().getQualifiedName();
                    }

                    if (calleeMethodName.equals("")) {
                      calleeMethodName = node.getType().resolveBinding().getQualifiedName();
                    }

                    VariableDeclarationFragment varDecFrag = null;
                    // we might need to add server upper level to cover all cases
                    if (node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
                      varDecFrag = (VariableDeclarationFragment) node.getParent();
                    } else if (node.getParent().getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
                      varDecFrag = (VariableDeclarationFragment) node.getParent().getParent();
                    } else if (node.getParent().getParent().getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
                      varDecFrag =
                          (VariableDeclarationFragment) node.getParent().getParent().getParent();
                    }
                    if (varDecFrag != null && varDecFrag.resolveBinding().isField()) {
                      fieldMethodMap.put(varDecFrag.getName().resolveBinding(), new MethodInfo(
                          calleeMethodName + ":"
                              + getMethodSignatureFromArguments(node.arguments()), calleeClassName));
                      // System.err.println("VARDEC " +
                      // varDecFrag.getName().getFullyQualifiedName());

                    } else {

                      nbClassInstanceCreation++;



                      int line = cuMethodUsageInfo.getLineNumber(node.getStartPosition());
                      String callerMethodName;
                      // for (String methodNameKey : returnStatementMethodLineMap.keySet()) {
                      // if (Integer.parseInt(methodNameKey.split(":")[0]) <= line
                      // && Integer.parseInt(methodNameKey.split(":")[1]) >= line) {
                      // callerMethodName = returnStatementMethodLineMap.get(methodNameKey);
                      // }
                      // }
                      callerMethodName = findMethodName(line);

                      // Get the innter class (if any)
                      String tCallerClassName = callerClassName;
                      int sLine = -1;
                      int eLine = Integer.MAX_VALUE;
                      for (String classNameKey : innerClassMap.keySet()) {
                        if (Integer.parseInt(classNameKey.split(":")[0]) <= line
                            && Integer.parseInt(classNameKey.split(":")[1]) >= line) {
                          if (Integer.parseInt(classNameKey.split(":")[0]) > sLine
                              && Integer.parseInt(classNameKey.split(":")[1]) < eLine) {
                            sLine = Integer.parseInt(classNameKey.split(":")[0]);
                            eLine = Integer.parseInt(classNameKey.split(":")[1]);
                            tCallerClassName = innerClassMap.get(classNameKey);
                            // System.out.println("inner " + callerMethodName + " " +
                            // tCallerClassName);
                          }
                        }
                      }

                      // System.out.print(methodSignatureBuilder.toString());
                      // System.out.println(" of " + callerMethodName + " in " + callerClassName);
                      List<Expression> arguments = node.arguments();
                      updateMethodUsageInformation(calleeClassName, tCallerClassName,
                          calleeMethodName + ":" + getMethodSignatureFromArguments(arguments),
                          callerMethodName, line);

                      // MethodUsage methodUsage;
                      // if (methodUsageContainer.containsKey(calleeClassName)) {
                      // methodUsage = methodUsageContainer.get(calleeClassName);
                      // } else {
                      // methodUsage = new MethodUsage(calleeClassName);
                      // }
                      // // the usage of addCallerMethod here is not good
                      // // there are many instances of the same MethodInfo for a method
                      //
                      // methodUsage.addCallerMethod(calleeMethodName + ":"
                      // + getMethodSignatureFromArguments(arguments), new MethodInfo(
                      // callerMethodName, tCallerClassName, line, cPath));
                      // // System.out.println("PUTTING " +
                      // // calleeMethodName+":"+getMethodSignature(arguments) + " INTO " +
                      // // calleeClassName);
                      // methodUsageContainer.put(calleeClassName, methodUsage);
                    }
                  }
                  return true;
                }

                public boolean visit(ConstructorInvocation node) {
                  int line = cuMethodUsageInfo.getLineNumber(node.getStartPosition());
                  String tCallerClassName = callerClassName;
                  int sLine = -1;
                  int eLine = Integer.MAX_VALUE;
                  for (String classNameKey : innerClassMap.keySet()) {
                    if (Integer.parseInt(classNameKey.split(":")[0]) <= line
                        && Integer.parseInt(classNameKey.split(":")[1]) >= line) {
                      if (Integer.parseInt(classNameKey.split(":")[0]) > sLine
                          && Integer.parseInt(classNameKey.split(":")[1]) < eLine) {
                        sLine = Integer.parseInt(classNameKey.split(":")[0]);
                        eLine = Integer.parseInt(classNameKey.split(":")[1]);
                        tCallerClassName = innerClassMap.get(classNameKey);
                        // System.out.println("inner " + node.getName().getFullyQualifiedName()
                        // +":" + getMethodSignature(node.arguments()) + " Found in " +
                        // callerMethodName + " " + tCallerClassName);
                      }
                    }
                  }
                  // System.out.println("INVOCATION " + node.resolveConstructorBinding().getName()
                  // + ":" + getMethodSignature(node.arguments()));
                  List<Expression> arguments = node.arguments();
                  String callerMethodName = findMethodName(line);
                  // the usage of addCallerMethod here is not good
                  // there are many instances of the same MethodInfo for a method
                  updateMethodUsageInformation(tCallerClassName, tCallerClassName, node
                      .resolveConstructorBinding().getName()
                      + ":"
                      + getMethodSignatureFromArguments(arguments), callerMethodName, line);

                  // MethodUsage methodUsage;
                  // if (methodUsageContainer.containsKey(tCallerClassName)) {
                  // methodUsage = methodUsageContainer.get(tCallerClassName);
                  // } else {
                  // methodUsage = new MethodUsage(tCallerClassName);
                  // }
                  // // List<Expression> arguments = node.arguments();
                  // methodUsage.addCallerMethod(node.resolveConstructorBinding().getName() + ":"
                  // + getMethodSignatureFromArguments(arguments), new MethodInfo(
                  // callerMethodName, tCallerClassName, line, cPath));
                  // // System.out.println("PUTTING " + node.resolveConstructorBinding().getName() +
                  // // ":"
                  // // + getMethodSignature(arguments) + " INTO " + callerMethodName + " OF "
                  // // + tCallerClassName);
                  // methodUsageContainer.put(tCallerClassName, methodUsage);

                  return true;
                }

                public boolean visit(SimpleName node) {

                  // if (node.getFullyQualifiedName().equals("archiver")) {
                  // System.out.println("SIMPLENAME " + node.getFullyQualifiedName());
                  // }

                  if (fieldMethodMap != null && fieldMethodMap.containsKey(node.resolveBinding())) {
                    int line = cuMethodUsageInfo.getLineNumber(node.getStartPosition());
                    String callerMethodName = findMethodName(line);
                    if (noContainedMethod) {
                      VariableDeclarationFragment varDecFrag = null;
                      // we might need to add server upper level to cover all cases
                      if (node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
                        varDecFrag = (VariableDeclarationFragment) node.getParent();
                      } else if (node.getParent().getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
                        varDecFrag = (VariableDeclarationFragment) node.getParent().getParent();
                      } else if (node.getParent().getParent().getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
                        varDecFrag =
                            (VariableDeclarationFragment) node.getParent().getParent().getParent();
                      }
                      if (varDecFrag != null && varDecFrag.resolveBinding().isField()) {
                        fieldMethodMap.put(varDecFrag.getName().resolveBinding(),
                            fieldMethodMap.get(node.resolveBinding()));
                        // System.err.println("PUTTING " + node.getFullyQualifiedName() + " INTO "
                        // + varDecFrag.getName().getFullyQualifiedName());

                      }
                    } else {
                      // System.out.println("WE FOUND " + node.getFullyQualifiedName() + " IN "
                      // + callerMethodName);
                      String tCallerClassName = callerClassName;
                      int sLine = -1;
                      int eLine = Integer.MAX_VALUE;
                      for (String classNameKey : innerClassMap.keySet()) {
                        if (Integer.parseInt(classNameKey.split(":")[0]) <= line
                            && Integer.parseInt(classNameKey.split(":")[1]) >= line) {
                          if (Integer.parseInt(classNameKey.split(":")[0]) > sLine
                              && Integer.parseInt(classNameKey.split(":")[1]) < eLine) {
                            sLine = Integer.parseInt(classNameKey.split(":")[0]);
                            eLine = Integer.parseInt(classNameKey.split(":")[1]);
                            tCallerClassName = innerClassMap.get(classNameKey);
                            // System.out.println("inner " + callerMethodName + " " +
                            // tCallerClassName);
                          }
                        }
                      }

                      String calleeClassName = fieldMethodMap.get(node.resolveBinding()).className;

                      updateMethodUsageInformation(calleeClassName, tCallerClassName,
                          fieldMethodMap.get(node.resolveBinding()).methodSignature,
                          callerMethodName, line);

                      // MethodUsage methodUsage;
                      // if (methodUsageContainer.containsKey(calleeClassName)) {
                      // methodUsage = methodUsageContainer.get(calleeClassName);
                      // } else {
                      // methodUsage = new MethodUsage(calleeClassName);
                      // }
                      // // the usage of addCallerMethod here is not good
                      // // there are many instances of the same MethodInfo for a method
                      // methodUsage.addCallerMethod(
                      // fieldMethodMap.get(node.resolveBinding()).methodSignature,
                      // new MethodInfo(callerMethodName, tCallerClassName, line, cPath));
                      // // System.out.println("PUTTING " + callerMethodName + " INTO "
                      // // + fieldMethodMap.get(node.resolveBinding()).methodSignature + " OF "
                      // // + calleeClassName + " AT " + line);
                      // methodUsageContainer.put(calleeClassName, methodUsage);
                    }

                  }
                  return true;
                }
              });
            }

            // break;

          }
        }
      }
    }


    // for (String key : parameterizedAbstractClass.keySet()) {
    // MethodUsage m = parameterizedAbstractClass.get(key);
    // System.out.println("ATTENTION ");
    // System.out.println(m.className);
    // for (String key2 : m.methodUsageMap.keySet()) {
    // System.out.println(key2 + " => " );
    // Set<MethodInfo> tSet = m.methodUsageMap.get(key2);
    // for (MethodInfo mInfo : tSet) {
    // System.out.println(mInfo.methodSignature + " AND " + mInfo.className);
    // }
    // }
    // System.out.println("=====");
    // }

    // for (String key : subClassInfo.keySet()) {
    // System.out.println("ATTENTION");
    // System.out.println("METHOD " + key + " IS IMPLEMENTATION ");
    // Set<SuperInterface> sInfSet = subClassInfo.get(key);
    // for (SuperInterface sInf : sInfSet) {
    // System.out.println(" OF " + sInf.className + " WITH TYPE " + sInf.parameterType);
    // for (String mName : sInf.overrideMethods) {
    // System.out.println("OVERRIDE " + mName);
    // }
    // }
    //
    //
    // System.out.println("==========");
    // }

    String packagePrefix = source_path.replaceAll("/", ".");
    this.tmpFileWriter.write("packagePrefix:" + packagePrefix + "\n");
    this.tmpFileWriter
        .write("size of methodUsageContainer " + methodUsageContainer.keySet().size());
    int countCassClass = 0;
    for (String key : methodUsageContainer.keySet()) {
      // if (key.equals("")) {
      if (key.contains(packagePrefix)) {
        countCassClass++;
      }
      MethodUsage temp = methodUsageContainer.get(key);
      this.tmpFileWriter.write("===========\n");
      this.tmpFileWriter.write("In class " + key + "\n");
      for (String keye : temp.methodUsageMap.keySet()) {
        Set<MethodInfo> tempList = temp.methodUsageMap.get(keye);
        for (MethodInfo methodInfo : tempList) {
          this.tmpFileWriter.write(new StringBuilder("++ METHOD ").append(keye)
              .append(" IS_USED_BY ").append(methodInfo.methodSignature).append(" OF ")
              .append(methodInfo.className).append(" AT ").append(methodInfo.lineNumber)
              .append("\n").toString());
        }
        // System.out.println("Method " + keye + " is called " + tempList.size() + " times");
      }
      // }
    }

    // System.out.println("nbClassNameEmpty : " + nbClassNameEmpty);
    // System.out.println("nbMethodNameEmpty : " + nbMethodNameEmpty);
    // System.out.println("nbClassInstanceCreation : " + nbClassInstanceCreation);
    this.tmpFileWriter.write("Number_of_class: " + countCassClass + "\n");
    this.tmpFileWriter.close();
    this.tempFileWriter.close();
    return methodUsageContainer;
  }


  /**
   * check if the direct variable of a conf. opiton is used in a conditional statement
   * 
   * @param packages
   * @param source_path
   * @param fullJavaFileName
   * @param methodName
   * @param variableName
   * @return
   * @throws JavaModelException
   */
  public int checkConditionaStatementDirectVariable(final IPackageFragment[] packages,
      final String source_path, String fullJavaFileName, final String methodName,
      final String variableName) throws JavaModelException {
    int nbConditionalStatements = 0;
    String javaFileName =
        fullJavaFileName
            .substring(fullJavaFileName.lastIndexOf(".") + 1, fullJavaFileName.length()) + ".java";
    System.out.println(javaFileName + " in checkConditionaStatementDirectVariable");
    for (IPackageFragment mypackage : packages) {
      if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
        // System.err.println("Package " + mypackage.getElementName());

        for (ICompilationUnit iCompilationUnit : mypackage.getCompilationUnits()) {
          // System.out.println(iCompilationUnit.getPath().toString());
          if (iCompilationUnit.getPath().toString().contains(source_path)
              && !iCompilationUnit.getElementName().contains("thrift/gen-java")
              && iCompilationUnit.getElementName().equals(javaFileName)) {
            // savediCompilationUnit = iCompilationUnit;
            ASTParser parser = ASTParser.newParser(AST.JLS8);
            parser.setSource(iCompilationUnit); // source from compilation unit
            parser.setResolveBindings(true);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            cuDirectVariableCheck = (CompilationUnit) parser.createAST(null);
            if (cuDirectVariableCheck != null && cuDirectVariableCheck.getPackage() != null
                && cuDirectVariableCheck.getPackage().getName() != null) {
              cuDirectVariableCheck.accept(new ASTVisitor() {
                @Override
                public boolean visit(MethodDeclaration node) {
                  if (node.getName().getFullyQualifiedName().equals(methodName)) {
                    startLineOfContaintedMethod =
                        cuDirectVariableCheck.getLineNumber(node.getStartPosition());
                    endLineOfContainedMethod =
                        cuDirectVariableCheck.getLineNumber(node.getStartPosition()
                            + node.getLength());
                  }
                  return true;
                }

                @Override
                public boolean visit(SimpleName node) {
                  // ASTNode temp =
                  if (cuDirectVariableCheck.getLineNumber(node.getStartPosition()) >= startLineOfContaintedMethod
                      && cuDirectVariableCheck.getLineNumber(node.getStartPosition()) <= endLineOfContainedMethod) {
                    if (node.getParent().getNodeType() == ASTNode.IF_STATEMENT) {

                      IfStatement stmt = (IfStatement) node.getParent();
                      Expression expr = stmt.getExpression();
                      if (cuMethodUsageInfo.getLineNumber(expr.getStartPosition()) <= cuMethodUsageInfo
                          .getLineNumber(node.getStartPosition())
                          && cuMethodUsageInfo.getLineNumber(expr.getStartPosition()
                              + expr.getLength()) >= cuMethodUsageInfo.getLineNumber(node
                              .getStartPosition() + node.getLength())) {
                        isInConditionalStatement = true;
                        // System.out.println(node + " has if parent " + node.getParent());
                      }
                    }
                    if (node.getParent().getParent().getNodeType() == ASTNode.IF_STATEMENT) {
                      IfStatement stmt = (IfStatement) node.getParent().getParent();
                      Expression expr = stmt.getExpression();
                      if (cuMethodUsageInfo.getLineNumber(expr.getStartPosition()) <= cuMethodUsageInfo
                          .getLineNumber(node.getStartPosition())
                          && cuMethodUsageInfo.getLineNumber(expr.getStartPosition()
                              + expr.getLength()) >= cuMethodUsageInfo.getLineNumber(node
                              .getStartPosition() + node.getLength())) {
                        isInConditionalStatement = true;
                        // System.out.println(node + " has if grandparent " +
                        // node.getParent().getParent());
                      }
                    }
                    if (node.getParent().getNodeType() == ASTNode.SWITCH_STATEMENT) {
                      SwitchStatement stmt = (SwitchStatement) node.getParent();
                      Expression expr = stmt.getExpression();
                      if (cuMethodUsageInfo.getLineNumber(expr.getStartPosition()) <= cuMethodUsageInfo
                          .getLineNumber(node.getStartPosition())
                          && cuMethodUsageInfo.getLineNumber(expr.getStartPosition()
                              + expr.getLength()) >= cuMethodUsageInfo.getLineNumber(node
                              .getStartPosition() + node.getLength())) {
                        isInConditionalStatement = true;
                        // System.out.println(node + " has switch parent " + node.getParent());
                      }
                    }
                    if (node.getParent().getParent().getNodeType() == ASTNode.SWITCH_STATEMENT) {
                      SwitchStatement stmt = (SwitchStatement) node.getParent().getParent();
                      Expression expr = stmt.getExpression();
                      if (cuMethodUsageInfo.getLineNumber(expr.getStartPosition()) <= cuMethodUsageInfo
                          .getLineNumber(node.getStartPosition())
                          && cuMethodUsageInfo.getLineNumber(expr.getStartPosition()
                              + expr.getLength()) >= cuMethodUsageInfo.getLineNumber(node
                              .getStartPosition() + node.getLength())) {
                        isInConditionalStatement = true;
                        // System.out.println(node + " has switch grandparent " +
                        // node.getParent().getParent());
                      }
                    }

                  }
                  return true;
                }

              });
            }
          }
        }
      }
    }
    if (isInConditionalStatement) {
      return 1;
    } else {
      return 0;
    }
    // return nbConditionalStatements;
  }

  private int getParamID(List<Expression> arguments, String nodeName) {
    int idParam = 0;
    for (int i = 0; i < arguments.size(); ++i) {
      // node.toString is the whole statement, we need to check it to get the order of
      // the configuration option in the invocation statement to do further analysis
      if (arguments.get(i).toString().contains(nodeName)) {

        idParam = i;
        break;
      }
    }
    return idParam;
  }

  PartIICCGNode pIINode;

  void traceField(PartIICCGNode parentPartIINode, String parentName, int level,
      final CompilationUnit cuLocal, ASTNode node, final String configName,
      final String containedMethod) { // this is a field
    toBeContinued = 0;
    // Utils.printResult4("FIELD");
    // return the name, method
    // Utils.printResult(level + " FIELD " + node + " CALLED BY " + parentName + " AT " +
    // cuLocal.getLineNumber(node.getStartPosition()));
    Utils.printResult(level + " FIELD " + node);
    // + " of method " + containedMethod + " of class " + savediCompilationUnit.getElementName() +
    // " at " + cuLocal.getLineNumber(node.getStartPosition())
    // + " type " + Utils.getASTNodeType(node) + " has parent " +
    // Utils.getASTNodeType(node.getParent()));
    // SimpleName name = null;
    gLevel = level;
    // TODO: not check the line number yet
    pIINode =
        new PartIICCGNode(node.getNodeType(), new MethodInfo(containedMethod,
            partIICallerClassName, cuLocal.getLineNumber(node.getStartPosition())), level);
    if (node.getNodeType() == ASTNode.FIELD_ACCESS) {
      FieldAccess tempNode = (FieldAccess) node;
      fieldName = tempNode.getName();
    } else if (node.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
      VariableDeclarationFragment tempNode = (VariableDeclarationFragment) node;
      fieldName = tempNode.getName();
    }
    countAssignment = 0;
    if (cuLocal != null && cuLocal.getPackage() != null && cuLocal.getPackage().getName() != null) {
      cuLocal.accept(new ASTVisitor() {
        @Override
        public boolean visit(SimpleName node) {
          if (node.resolveBinding() == fieldName.resolveBinding()) {
            // Utils.printResult("Analyzing " + node.getFullyQualifiedName());
            ASTNode parent = node.getParent();
            // we need to check whether the field is initialized by
            // a value different from a configuraiton option
            if (parent.getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION) {
              if (((VariableDeclarationFragment) parent).getInitializer() == null) {
                countAssignment = 1;
              }
            }
            boolean check = false;
            if (parent.getNodeType() == ASTNode.METHOD_INVOCATION) {
              try {
                toBeContinued = 1;
                traceMethodInvocation(pIINode, node.toString(), gLevel + 1, 0, cuLocal, 0,
                    node.getParent(), configName, containedMethod,
                    getParamID(((MethodInvocation) parent).arguments(), node.toString()));
              } catch (JavaModelException e) {
                e.printStackTrace();
              }
              check = true;
            } else if (parent.getNodeType() == ASTNode.FIELD_ACCESS) {
              if (parent.getParent().getNodeType() == ASTNode.ASSIGNMENT) {
                // Utils.printResult("This variable is assigned " + countAssignment++ + " times");
                check = true;
              } else if (parent.getParent().getNodeType() == ASTNode.PREFIX_EXPRESSION) {
                try {
                  toBeContinued = 1;
                  traceParentStatement(pIINode, node.toString(), gLevel + 1, cuLocal, 1, parent,
                      configName, containedMethod);
                } catch (JavaModelException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
              } else if (parent.getParent().getNodeType() == ASTNode.INFIX_EXPRESSION) {
                // Utils.printResult("Found it " + node.getFullyQualifiedName() + " at " +
                // cuLocal.getLineNumber(node.getStartPosition())
                // + " in a " + Utils.getASTNodeType(parent) + " in a " +
                // Utils.getASTNodeType(parent.getParent()));
                try {
                  toBeContinued = 1;
                  traceParentStatement(pIINode, node.toString(), gLevel + 1, cuLocal, 1, parent,
                      configName, containedMethod);
                } catch (JavaModelException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
              } else if (parent.getParent().getNodeType() == ASTNode.METHOD_INVOCATION) {
                try {
                  toBeContinued = 1;
                  traceMethodInvocation(
                      pIINode,
                      node.toString(),
                      gLevel + 1,
                      0,
                      cuLocal,
                      0,
                      parent.getParent(),
                      configName,
                      containedMethod,
                      getParamID(((MethodInvocation) parent.getParent()).arguments(),
                          node.toString()));
                } catch (JavaModelException e) {
                  e.printStackTrace();
                }
              } else if (parent.getParent().getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
                toBeContinued = 1;
                traceClassInstanceCreation(
                    pIINode,
                    node.toString(),
                    gLevel + 1,
                    cuLocal,
                    parent.getParent(),
                    configName,
                    containedMethod,
                    getParamID(((ClassInstanceCreation) parent.getParent()).arguments(),
                        parent.toString()));

              } else if (parent.getParent().getNodeType() == ASTNode.RETURN_STATEMENT) {
                toBeContinued = 1;
                traceReturnStatement(pIINode, node.toString(), gLevel + 1, cuLocal,
                    parent.getParent(), configName, containedMethod);
              } else if (parent.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
                // we do not need to do anything
              } else {
                // Utils.printResult("Inisde FIELD_ACCESS Found it " + node.getFullyQualifiedName()
                // + " is " + Utils.getASTNodeType(node) + " at " +
                // cuLocal.getLineNumber(node.getStartPosition())
                // + " in a " + Utils.getASTNodeType(parent) + " in a " +
                // Utils.getASTNodeType(parent.getParent()));
              }

            } else if (parent.getNodeType() == ASTNode.ASSIGNMENT) {
              // we do not need to do anything
            } else if (parent.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
              // we do not need to do anything
            } else if (parent.getNodeType() == ASTNode.INFIX_EXPRESSION) {
              try {
                toBeContinued = 1;
                traceParentStatement(pIINode, node.toString(), gLevel + 1, cuLocal, 1, node,
                    configName, containedMethod);
              } catch (JavaModelException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            } else if (parent.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
              toBeContinued = 1;
              traceClassInstanceCreation(pIINode, node.toString(), gLevel + 1, cuLocal, parent,
                  configName, containedMethod,
                  getParamID(((ClassInstanceCreation) parent).arguments(), node.toString()));
            } else if (parent.getNodeType() == ASTNode.CONDITIONAL_EXPRESSION) {
              try {
                toBeContinued = 1;
                traceParentStatement(pIINode, node.toString(), gLevel + 1, cuLocal, 1, node,
                    configName, containedMethod);
              } catch (JavaModelException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            } else if (parent.getNodeType() == ASTNode.IF_STATEMENT) {
              toBeContinued = 2;
              // Utils.printResult3("FIELD_IF_STATEMENT " + configName +
              // " in a IfStatement of method " + containedMethod + " of class " +
              // savediCompilationUnit.getElementName());
              conditionalStmtUsage.add(new ConditionalStmtUsage(cuLocal.getJavaElement()
                  .getElementName(), "", configName));
            } else if (parent.getNodeType() == ASTNode.RETURN_STATEMENT) {
              toBeContinued = 1;
              traceReturnStatement(pIINode, node.toString(), gLevel + 1, cuLocal, parent,
                  configName, containedMethod);
            } else if (parent.getNodeType() == ASTNode.ARRAY_CREATION) {
              Utils.printResult3("ARRAY_CREATION " + configName);
              if (parent.getParent().getNodeType() == ASTNode.ASSIGNMENT) {
                Utils.printResult3(" in an assginment statement");
                // we need to continute analyze the field here
                Assignment assign = (Assignment) parent.getParent();
                Expression leftHandSide = assign.getLeftHandSide();
                String leftHandSideType = Utils.getExpressionRawType(leftHandSide);
                // Utils.printResult(leftHandSideType);
                if (leftHandSideType.equals(Utils.FIELDACCESS)) {
                  // Utils.printResult("We need to parsefield " + leftHandSide.toString());
                  toBeContinued = 1;
                  traceField(pIINode, node.toString(), gLevel + 1, cuLocal, leftHandSide,
                      configName, containedMethod);
                } else {
                  // Utils.printResult("We need to parsevariablefield " + leftHandSide.toString());
                  toBeContinued = 1;
                  traceVariableField(pIINode, node.toString(), gLevel + 1, cuLocal, leftHandSide,
                      configName, containedMethod);
                }
              } else {
                Utils.printResult3("IN_OTHER_THING");
              }
            } else if (parent.getNodeType() == ASTNode.SUPER_CONSTRUCTOR_INVOCATION) {
              toBeContinued = 1;
              SuperConstructorInvocation superClass = (SuperConstructorInvocation) parent;
              traceSuperConstructorInvocation(pIINode, node.toString(), gLevel + 1, cuLocal,
                  superClass, configName, containedMethod,
                  getParamID(superClass.arguments(), node.toString()));
            } else {
              Utils.printResult3("Found it " + node.getFullyQualifiedName() + " is "
                  + Utils.getASTNodeType(node) + " at "
                  + cuLocal.getLineNumber(node.getStartPosition()) + " in a "
                  + Utils.getASTNodeType(parent) + " in a "
                  + Utils.getASTNodeType(parent.getParent()));
            }
            if (!check) {
            }
          }
          return true;
        }

      });
    }
    if (toBeContinued == 0) {
      Utils.printResult4("STOP_ANALYZE_FIELD");
    } else if (toBeContinued == 2) {
      Utils.printResult4("STOPIF");
    }
  }

  boolean noContainedMethod;

  String findMethodName(int statementLine) {
    // String result = "";
    noContainedMethod = false;
    for (int i = 1; i < methodNames.size(); ++i) {
      if (startLineofMethods.get(i) <= statementLine && endLineofMethods.get(i) >= statementLine) {
        return methodNames.get(i);
      }
    }
    noContainedMethod = true;
    return methodNames.get(0); // the statement is not used in any method. It can be field
                               // declaration or a block of code like static { }
  }


  void traceVariableField(PartIICCGNode parentPartIINode, String parentName, int level,
      final CompilationUnit cuLocal, ASTNode node, final String configName,
      final String containedMethod) {// this might be a field or a local variable
    toBeContinued = 0;
    // Utils.printResult4("VARIABLEFIELD");
    correctContainedMethod = findMethodName(cuLocal.getLineNumber(node.getStartPosition()));
    // Utils.printResult(level + " VARIABLEFIELD " + node + " CALLED BY " + parentName + " AT " +
    // cuLocal.getLineNumber(node.getStartPosition()));
    Utils.printResult(level + " VARIABLEFIELD " + node);
    // " for " + configName
    // + " of method " + containedMethod + " of class " + savediCompilationUnit.getElementName() +
    // " type " + Utils.getASTNodeType(node) + " "
    // + " at " + cuLocal.getLineNumber(node.getStartPosition()));
    variableFieldName = null;
    qVariableFieldName = null;
    gLevel = level;
    pIINode =
        new PartIICCGNode(node.getNodeType(), new MethodInfo(containedMethod,
            partIICallerClassName, cuLocal.getLineNumber(node.getStartPosition())), level);
    fieldAccess = null;
    if (visitedVariableFiled.contains(node)) {
      Utils.printResult3("VISITED");
    } else {
      visitedVariableFiled.add(node);
    }
    if (node instanceof Expression) {
      Expression expr = (Expression) node;
      if ((expr instanceof SimpleName)) {
        variableFieldName = (SimpleName) expr;
        Utils.printResult3("NOTNOT " + Utils.getExpressionRawType((Expression) node));
      } else if ((expr instanceof QualifiedName)) {
        qVariableFieldName = (QualifiedName) expr;
        Utils.printResult3("HEHE " + expr);
      } else if (expr instanceof FieldAccess) {
        fieldAccess = (FieldAccess) expr;
        variableFieldName = fieldAccess.getName();
        fieldAccess = null;
        // Utils.printResult3("HAHA " + variableFieldName);
      } else {
        // Utils.printResult4("DIFFERENT");
      }
    } else if (node.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION) { // parameter
      variableFieldName = ((SingleVariableDeclaration) node).getName();
      Utils.printResult3("PARAMETER " + variableFieldName);
    } else {
      // Utils.printResult4("DIFFERENT2");
    }

    fcount = 0;
    visitedSimpleName.clear();
    if (cuLocal != null && cuLocal.getPackage() != null && cuLocal.getPackage().getName() != null) {

      cuLocal.accept(new ASTVisitor() {
        // public boolean visit(VariableDeclarationFragment node) {
        // return true;
        // }

        @Override
        public boolean visit(SimpleName node) {
          if (variableFieldName != null) {
            StringBuilder tempStrBuilder =
                (new StringBuilder(node.getFullyQualifiedName())).append(
                    cuLocal.getLineNumber(node.getStartPosition())).append(
                    node.getParent().toString());
            if (!visitedSimpleName.contains(tempStrBuilder.toString())
                && node.resolveBinding() == variableFieldName.resolveBinding()) {
              visitedSimpleName.add(tempStrBuilder.toString());
              // visitedSimpleName.add(e)
              // Utils.printResult4("variableFieldName 22 " + node + " " + fcount++ + " at " +
              // cuLocal.getLineNumber(node.getStartPosition()));
              ASTNode parent = node.getParent();
              if (parent.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
                // we might check if there is no declaration, => that variable is decralared in the
                // super class
              } else if (parent.getNodeType() == ASTNode.METHOD_INVOCATION) {
                try {
                  // Utils.printResult("METHOD_INVOCATION Calling analyzeMethodInvocation from analyzeVariableField");
                  toBeContinued = 1;
                  traceMethodInvocation(pIINode, node.toString(), gLevel + 1, 0, cuLocal, 0,
                      node.getParent(), configName, correctContainedMethod,
                      getParamID(((MethodInvocation) parent).arguments(), node.toString()));
                } catch (JavaModelException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
                // check = true;
              } else if (parent.getNodeType() == ASTNode.INFIX_EXPRESSION) {
                try {
                  Utils
                      .printResult3("INFIX_EXPRESSION Calling analyzeParentStatement from analyzeVariableField "
                          + ((InfixExpression) parent).getOperator()
                          + " "
                          + ((InfixExpression) parent).getLeftOperand()
                          + " : "
                          + ((InfixExpression) parent).getRightOperand());
                  toBeContinued = 1;
                  traceParentStatement(pIINode, node.toString(), gLevel + 1, cuLocal, 1, node,
                      configName, containedMethod);
                } catch (JavaModelException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
              } else if (parent.getNodeType() == ASTNode.RETURN_STATEMENT) {
                Utils
                    .printResult3("RETURN_STATEMENT Calling analyzeReturnStatement from analyzeVariableField");
                toBeContinued = 1;
                traceReturnStatement(pIINode, node.toString(), gLevel + 1, cuLocal, parent,
                    configName, containedMethod);
              } else if (parent.getNodeType() == ASTNode.IF_STATEMENT) {
                // Utils.printResult3("FIELD_IF_STATEMENT " + configName +
                // " in a IfStatement of method " + containedMethod + " of class " +
                // savediCompilationUnit.getElementName());
              } else if (parent.getNodeType() == ASTNode.FIELD_ACCESS) {
                if (parent.getParent().getNodeType() == ASTNode.METHOD_INVOCATION) {
                  try {
                    Utils
                        .printResult3("FIELD_ACCESS-METHOD_INVOCATION Calling analyzeMethodInvocation from analyzeVariableField");
                    toBeContinued = 1;
                    traceMethodInvocation(
                        pIINode,
                        node.toString(),
                        gLevel + 1,
                        0,
                        cuLocal,
                        0,
                        parent.getParent(),
                        configName,
                        containedMethod,
                        getParamID(((MethodInvocation) parent.getParent()).arguments(),
                            node.toString()));
                  } catch (JavaModelException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
                } else if (parent.getParent().getNodeType() == ASTNode.CAST_EXPRESSION) {
                  try {
                    Utils
                        .printResult3("CAST_EXPRESSION Calling analyzeParentStatement from analyzeVariableField");
                    toBeContinued = 1;
                    traceParentStatement(pIINode, node.toString(), gLevel + 1, cuLocal, 1, parent,
                        configName, containedMethod);
                  } catch (JavaModelException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
                } else if (parent.getParent().getNodeType() == ASTNode.ASSIGNMENT) {
                  // Utils.printResult("This variable is assigned " + countAssignment++ + " times");
                  // we need to call how assignments a variable has
                }
              } else if (parent.getNodeType() == ASTNode.ASSIGNMENT) {
                // we need to call how assignments a variable has
                Assignment assign = (Assignment) parent;
                if (node.getLocationInParent().getId().equals("rightHandSide")) {
                  Utils.printResult3("assigned to " + assign.getLeftHandSide() + " "
                      + node.getLocationInParent().getId());
                  toBeContinued = 1;
                  traceVariableField(pIINode, node.toString(), gLevel + 1, cuLocal,
                      assign.getLeftHandSide(), "testtest", containedMethod);
                }

              } else if (parent.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
                Utils
                    .printResult3("CLASS_INSTANCE_CREATION Calling analyzeMethodInvocation from analyzeVariableField");
                toBeContinued = 1;
                traceClassInstanceCreation(pIINode, node.toString(), gLevel + 1, cuLocal, parent,
                    configName, containedMethod,
                    getParamID(((ClassInstanceCreation) parent).arguments(), node.toString()));
              } else if (parent.getNodeType() == ASTNode.PREFIX_EXPRESSION) {
                try {
                  Utils
                      .printResult3("PREFIX_EXPRESSION Calling analyzeMethodInvocation from analyzeVariableField");
                  toBeContinued = 1;
                  traceParentStatement(pIINode, node.toString(), gLevel + 1, cuLocal, 1, node,
                      configName, containedMethod);
                } catch (JavaModelException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
              } else if (parent.getNodeType() == ASTNode.CONDITIONAL_EXPRESSION) {
                try {
                  Utils
                      .printResult3("CONDITIONAL_EXPRESSION Calling analyzeMethodInvocation from analyzeVariableField");
                  toBeContinued = 1;
                  traceParentStatement(pIINode, node.toString(), gLevel + 1, cuLocal, 1, node,
                      configName, containedMethod);
                } catch (JavaModelException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
              } else if (parent.getNodeType() == ASTNode.ARRAY_CREATION) {
                Utils.printResult2("ARRAY_CREATION " + configName);
                if (parent.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
                  Utils.printResult3(" in an assginment statement");
                  // we need to continute analyze the field here
                  VariableDeclarationFragment varDec =
                      (VariableDeclarationFragment) parent.getParent();


                  // String leftHandSideType = Utils.getExpressionRawType(leftHandSide);
                  // Utils.printResult(leftHandSideType);
                  // if (leftHandSideType.equals(Utils.FIELDACCESS)) {
                  // Utils.printResult("We need to parsefield " + leftHandSide.toString());
                  // analyzeField(pIINode, (FieldAccess)leftHandSide, configName, containedMethod);
                  // } else {
                  // Utils.printResult("We need to parsevariablefield " + leftHandSide.toString());
                  Utils
                      .printResult3("VARIABLE_DECLARATION_FRAGMENT Calling analyzeVariableField from analyzeVariableField");
                  traceVariableField(pIINode, node.toString(), gLevel + 1, cuLocal,
                      varDec.getName(), configName, containedMethod);
                  // }
                } else {
                  Utils.printResult3("IN_OTHER_THING");
                }
              } else if (parent.getNodeType() == ASTNode.ARRAY_ACCESS) {
                // TODO not sure what do do
              } else if (parent.getNodeType() == ASTNode.ARRAY_TYPE) {

              } else if (parent.getNodeType() == ASTNode.QUALIFIED_NAME) {

              } else {
                // Utils.printResult("Found it " + node.getFullyQualifiedName() + " is " +
                // parent.getNodeType() + " " + Utils.getASTNodeType(node) + " at " +
                // cuLocal.getLineNumber(node.getStartPosition())
                // + " in a " + Utils.getASTNodeType(parent) + " in a " +
                // Utils.getASTNodeType(parent.getParent()));
              }
            }
          }
          return true;
        }

        // public boolean visit(QualifiedName node) {
        // // if (node.resolveBinding() == qVariableFieldName.resolveBinding()) {
        // // ASTNode parent = node.getParent();
        // // Utils.printResult("QFound it " + node.getFullyQualifiedName() + " is " +
        // Utils.getASTNodeType(node) + " at " + cuLocal.getLineNumber(node.getStartPosition())
        // // + " in a " + Utils.getASTNodeType(parent) + " in a " +
        // Utils.getASTNodeType(parent.getParent()));
        // // }
        // return true;
        // }

        // public boolean visit(FieldAccess node) {
        // if (fieldAccess != null) {
        // if (node.resolveFieldBinding() == fieldAccess.resolveFieldBinding()) {
        // ASTNode parent = node.getParent();
        // // if (parent.getNodeType() == ASTNode.INFIX_EXPRESSION) {
        // // try {
        // // analyzeParentStatement(node.toString(), gLevel+1, cuLocal, 1, node, configName,
        // containedMethod);
        // // } catch (JavaModelException e) {
        // // // TODO Auto-generated catch block
        // // e.printStackTrace();
        // // }
        // // } else if (parent.getNodeType() == ASTNode.RETURN_STATEMENT) {
        // // analyzeReturnStatement(pIINode, node.toString(), gLevel + 1, cuLocal, parent,
        // configName, containedMethod);
        // // } else if (parent.getNodeType() == ASTNode.ASSIGNMENT) {
        // // //
        // // } else
        // // Utils.printResult("Found it " + node.getName().getFullyQualifiedName() + " is " +
        // parent.getNodeType() + " " + Utils.getASTNodeType(node) + " at " +
        // cuLocal.getLineNumber(node.getStartPosition())
        // // + " in a " + Utils.getASTNodeType(parent) + " in a " +
        // Utils.getASTNodeType(parent.getParent()));
        // }
        // }
        // return true;
        // }
      });
    }

    if (toBeContinued == 0) {
      Utils.printResult4("STOP_ANALYZE_VAR_FIELD");
    }
  }


  void traceMethodInvocation(PartIICCGNode parentPartIINode, String parentName, int level,
      int direct, final CompilationUnit cuLocal, int recursive, ASTNode node,
      final String configName, final String containedMethod, final int idParam)
      throws JavaModelException {
    typeOfParents[ASTNode.METHOD_INVOCATION] += 1;
    toBeContinued = 0;

    MethodInvocation mInvocation = (MethodInvocation) node;
    methodName = mInvocation.getName().getFullyQualifiedName();
    methodSignature = getMethodSignatureFromArguments(mInvocation.arguments());
    String methodNameSig = (new StringBuilder(methodName).append(methodSignature)).toString();
    if (visitedMethods.contains(methodNameSig)) {
      Utils.printResult4("==VISITED_METHOD==");
      return; // return here to avoid redundant computation steps
    } else {
      visitedMethods.add(methodNameSig);
    }
    // Utils.printResult5("METHOD_INVOCATION_START");

    gLevel = level;
    pIINode =
        new PartIICCGNode(node.getNodeType(), new MethodInfo(containedMethod,
            partIICallerClassName, cuLocal.getLineNumber(node.getStartPosition())), level);
    ASTNode parent = node.getParent();
    if (recursive == 0) {
      // Utils.printResult4("-------------");
    } else if (recursive == 1) {
      // Utils.printResult4("RECURSIVELY ");
    }

    boolean isVoid = false;

    String qualifiedClassName = "niil";
    String className = "niil";
    Utils.printResult4(className);
    boolean foundClass = false;

    if (mInvocation.resolveMethodBinding() != null) {
      qualifiedClassName =
          mInvocation.resolveMethodBinding().getDeclaringClass().getQualifiedName();
      className = mInvocation.resolveMethodBinding().getDeclaringClass().getName() + ".java";

      if (mInvocation.resolveMethodBinding().getReturnType().getQualifiedName().equals("void")
          && !"org.apache.commons.logging.Log".equals(mInvocation.resolveMethodBinding()
              .getDeclaringClass().getQualifiedName())) {
        // Utils.printResult5(methodName);
        isVoid = true;
      }
    }
    // Utils.printResult2("OUTPUT_METHOD_NAME ");


    // + " is called in method " + containedMethod + " of class " +
    // savediCompilationUnit.getElementName());
    // return;
    // }

    // Utils.printResult4("mInvocation " + mInvocation.getName().getFullyQualifiedName());

    Utils.printResult4(methodName + " : " + methodSignature);
    if (cuLocal != null && cuLocal.getPackage() != null && cuLocal.getPackage().getName() != null) {
      cuLocal.accept(new ASTVisitor() {
        @Override
        public boolean visit(MethodDeclaration node) {
          Utils.printResult4(node.getName() + " :: "
              + getMethodSignatureFromParameters(node.parameters()));
          if (node.getName().getFullyQualifiedName().equals(methodName)) {
            if (getMethodSignatureFromParameters(node.parameters()).equals(methodSignature)) {
              if (idParam < node.parameters().size()) {
                // Utils.printResult5("FOUND IN THE SAME CLASS AT " +
                // cuLocal.getLineNumber(node.getStartPosition()));
                toBeContinued = 1;
                ASTNode paramNode = (ASTNode) node.parameters().get(idParam);
                // Utils.printResult5("ANALYZE VAR " + paramNode);
                traceVariableField(pIINode, node.toString(), gLevel + 1, cuLocal, paramNode,
                    configName, containedMethod);
              }
            }
          }
          return true;
        }
      });
    }


    {
      for (IPackageFragment mypackage : globalPackages) {
        if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
          // if (foundClass) //TODO: check the correctness
          // break;

          // Utils.printResult("Package " + mypackage.getElementName());
          // packageName = mypackage.getElementName();

          for (ICompilationUnit iCompilationUnit : mypackage.getCompilationUnits()) {
            // Utils.printResult(iCompilationUnit.getPath().toString());
            // System.err.println(global_source_path + " : " + global_source_path);
            if (iCompilationUnit.getPath().toString().contains(global_source_path)
                && !iCompilationUnit.getElementName().contains("thrift/gen-java")
                // && iCompilationUnit.getElementName().equals("HConstants.java")) {
                && iCompilationUnit.getElementName().equals(className)) {
              // if (foundClass) //TODO: check the correctness
              // break;
              foundClass = true;
              // Utils.printResult4("WEFOUND" + className);

              ASTParser parser = ASTParser.newParser(AST.JLS8);
              parser.setSource(iCompilationUnit); // source from compilation unit
              parser.setResolveBindings(true);
              parser.setKind(ASTParser.K_COMPILATION_UNIT);

              cuAnalyzeMethodInvocation = (CompilationUnit) parser.createAST(null);



              // Utils.printResult("Parsing " + iCompilationUnit.getElementName());

              if (cuAnalyzeMethodInvocation != null
                  && cuAnalyzeMethodInvocation.getPackage() != null
                  && cuAnalyzeMethodInvocation.getPackage().getName() != null) {

                cuAnalyzeMethodInvocation.accept(new ASTVisitor() {

                  @Override
                  public boolean visit(MethodDeclaration node) {


                    if (node.getName().getFullyQualifiedName().equals(methodName)) {
                      // Utils.printResult4(methodName + " : " + node.getName() + " : " +
                      // getMethodSignatureFromParameters(node.parameters())
                      // + " : " + methodSignature);
                      if (getMethodSignatureFromParameters(node.parameters()).equals(
                          methodSignature)
                          && (node.parameters().size() > idParam)) {
                        // Utils.printResult4("Analyzing " + node.parameters().get(idParam));
                        ASTNode paramNode = (ASTNode) node.parameters().get(idParam);
                        // Utils.printResult("Calling analyzeVariableField from analyzeMethodInvocation");
                        Utils.printResult4(gLevel + " METHOD_INVOCATION_THOUGH_ANOTHER_VARIABLE "
                            + node.getName().getFullyQualifiedName());
                        toBeContinued = 1;
                        traceVariableField(pIINode, node.toString(), gLevel + 1,
                            cuAnalyzeMethodInvocation, paramNode, configName, containedMethod);
                      }
                    }
                    return true;
                  }

                });
              }
            }
          }
        }
      }
    }

    if (hasOracleFolder && !foundClass) {
      boolean foundInOtherModules = false;
      File folder = new File((new StringBuilder(oracleFolder).append("/modules").toString()));
      if (folder != null) {
        File[] listOfFiles = folder.listFiles();
        String line = null;
        if (listOfFiles != null) {
          for (int i = 0; i < listOfFiles.length; ++i) {
            if (foundInOtherModules)
              break;
            File file = listOfFiles[i];
            if (file.isFile() && file.getName().endsWith(".txt")) {
              try {
                BufferedReader bufferedReader =
                    new BufferedReader(new FileReader(folder + "/" + file.getName()));

                // use the readLine method of the BufferedReader to read one line at a time.
                // the readLine method returns null when there is nothing else to read.
                while ((line = bufferedReader.readLine()) != null) {
                  if (line.trim().equals(qualifiedClassName)) {
                    Utils.printResult4("GOTIT " + file.getName() + " " + qualifiedClassName);

                    foundInOtherModules = true;
                    break;
                  }
                }
                bufferedReader.close();
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        }
      }
      if (!foundInOtherModules) {
        // Utils.printResult5("HERE");
        if (parent.getNodeType() == ASTNode.IF_STATEMENT) {
          foundClass = true;
          // Utils.printResult("Calling analyzeParentStatement from analyzeMethodInvocation");
          toBeContinued = 1;
          traceParentStatement(pIINode, node.toString(), level + 1, cuLocal, 0, node, configName,
              containedMethod);
        }
        // if (isVoid && (direct == 1 || gLevel == 1)) {
        if (isVoid) {
          // Utils.printResult5("REAL EXTERNAL LIB " + methodName + " OF " + qualifiedClassName);
          // MethodLibraryUsage mLibUsage = new MethodLibraryUsage(qualifiedClassName, methodName,
          // configName);
          // methodLibUsage.add(mLibUsage);
          if (!qualifiedClassName.equals("org.apache.commons.logging.Log")) {
            typeOfParents[99] += 1;
            MethodLibraryUsage mLibUsage =
                new MethodLibraryUsage(qualifiedClassName, methodName, configName);

            // boolean isExisting = false;
            // for (MethodLibraryUsage tmpLibUsage : methodLibUsage) {
            // if (mLibUsage.equals(tmpLibUsage)) {
            // isExisting = true;
            // break;
            // }
            // }
            // if (!isExisting)
            // Utils.printResult5("HERE222");
            methodLibUsage.add(mLibUsage);
          }
        } else {
          if (!qualifiedClassName.equals("org.apache.commons.logging.Log")) {
            // Utils.printResult4("EXTERNALLIB " + methodName + " " + qualifiedClassName);
          }
        }


      }
    }

    // Utils.printResult(level + " METHOD_INVOCATION " + node + " CALLED BY " + parentName + " AT "
    // + cuLocal.getLineNumber(node.getStartPosition()));
    Utils.printResult(level + " METHOD_INVOCATION " + node);
    // + configName + " is the " + idParam + "th parameter of "
    // + " of " + methodName + " which has signature " + methodSignature
    // + " from class " + mInvocation.resolveMethodBinding().getDeclaringClass().getQualifiedName()
    // + " return " + " in " + savediCompilationUnit.getElementName() + " node " + node);
    //
    // System.err.println(className + " :: " + className);
    if (parent.getNodeType() == ASTNode.METHOD_INVOCATION) {
      Utils.printResult3("CALLINGAGAIN " + parent);
      foundClass = true;
      Utils.printResult3("Calling analyzeMethodInvocation from analyzeMethodInvocation");
      toBeContinued = 1;
      traceMethodInvocation(pIINode, node.toString(), level + 1, 0, cuLocal, 1, parent, configName,
          containedMethod, getParamID(((MethodInvocation) parent).arguments(), node.toString())); // nhan
    } else if (parent.getNodeType() == ASTNode.CONSTRUCTOR_INVOCATION) {
      Utils.printResult3("CALLINGCONSTRUCTOR" + parent);
      foundClass = true;
      Utils.printResult3("Calling analyzeConstructorInvocation from analyzeMethodInvocation");
      toBeContinued = 1;
      traceConstructorInvocation(pIINode, node.toString(), level + 1, cuLocal, parent, configName,
          containedMethod,
          getParamID(((ConstructorInvocation) parent).arguments(), node.toString()));
    } else if (parent.getNodeType() == ASTNode.RETURN_STATEMENT) {
      Utils.printResult3("CALLINGRETURN " + parent);
      foundClass = true;
      Utils.printResult3("Calling analyzeReturnStatement from analyzeMethodInvocation");
      toBeContinued = 1;
      traceReturnStatement(pIINode, node.toString(), level + 1, cuLocal, parent, configName,
          containedMethod);
    } else if (parent.getNodeType() == ASTNode.INFIX_EXPRESSION) {
      Utils
          .printResult3("CALLINGINFIX calling analyzeParentStatement from analyzeMethodInvocation");
      foundClass = true;
      toBeContinued = 1;
      traceParentStatement(pIINode, node.toString(), level + 1, cuLocal, 1, node, configName,
          containedMethod);
    } else if (savediCompilationUnit.getElementName().equals(className)) {
      Utils.printResult3("THESAMECLASS");
      foundClass = true;
    } else if (parent.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
      foundClass = true;
      Utils.printResult3("CALLINGCLASSINSTANCE from analyzeMethodInvocation");
      toBeContinued = 1;
      traceClassInstanceCreation(pIINode, node.toString(), level + 1, cuLocal, parent, configName,
          containedMethod,
          getParamID(((ClassInstanceCreation) parent).arguments(), node.toString()));
    } else if (parent.getNodeType() == ASTNode.CAST_EXPRESSION) {
      foundClass = true;
      Utils
          .printResult3("CAST_EXPRESSION calling analyzeParentStatement from analyzeMethodInvocation");
      toBeContinued = 1;
      traceParentStatement(pIINode, node.toString(), level + 1, cuLocal, 1, node, configName,
          containedMethod);
    } else if (parent.getNodeType() == ASTNode.ASSIGNMENT) {
      toBeContinued = 1;
      Utils.printResult4("METHOD IS ASSIGNED TO " + ((Assignment) parent).getLeftHandSide());
    } else if (parent.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
      toBeContinued = 1;
      Utils.printResult4("VAR_DEC_FRAG OF METHODINVOCATION");
    } else if (parent.getNodeType() == ASTNode.FIELD_ACCESS) {
      toBeContinued = 1;
      Utils.printResult4("FIELDACESS OF METHODINVOCATION");
    } else if (parent.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
      toBeContinued = 1;
      ExpressionStatement expStmt = (ExpressionStatement) parent;
      // expStm
      Utils.printResult4("EXPRESSION_STATEMENT " + expStmt.getExpression() + " TYPE "
          + Utils.getASTNodeType(expStmt.getExpression()) + " OF "
          + Utils.getASTNodeType(parent.getParent()) + " "
          + mInvocation.getLocationInParent().getId());
    } else if (parent.getNodeType() == ASTNode.IF_STATEMENT) {
      toBeContinued = 1;
      // Utils.printResult4("IF_STATEMENT OF METHODINVOCATION");
      conditionalStmtUsage.add(new ConditionalStmtUsage(cuLocal.getJavaElement().getElementName(),
          "", configName));
    } else if (parent.getNodeType() == ASTNode.ARRAY_CREATION) {
      toBeContinued = 1;
      Utils.printResult4("ARRAYCREATION " + parent);
      // methodLibUsage.add(new MethodLibraryUsage(cuLocal.getJavaElement().getElementName(), "",
      // configName)); //temporarily
    } else {
      Utils.printResult4("NOWHERE TO GO " + Utils.getASTNodeType(parent));
    }

    if (toBeContinued == 0) {
      Utils.printResult4("STOP_ANALYZE_METHOD_INVO");
    }
  }

  void traceClassInstanceCreation(PartIICCGNode parentPartIINode, String parentName, int level,
      final CompilationUnit cuLocal, ASTNode node, String configName, String containedMethod,
      int idParam) {
    // Utils.printResult4(level + " CLASS_INSTANCE_CREATION");
    ClassInstanceCreation cInstance = (ClassInstanceCreation) node;
    // Utils.printResult(level + " CLASS_INSTANCE_CREATION " + node + " CALLED BY " + parentName +
    // " AT " + cuLocal.getLineNumber(node.getStartPosition()));
    Utils.printResult(level + " CLASS_INSTANCE_CREATION " + node);
    // "called by " + parentName + "  :  " + configName + " is the " + idParam + "th parameter of "
    // + " of new class instance of " +
    // cInstance.resolveConstructorBinding().getDeclaringClass().getQualifiedName() +
    // " which has signature "
    // + getMethodSignature(cInstance.arguments()) + " is called in method " + containedMethod +
    // " of class " + savediCompilationUnit.getElementName());
    // " " + Utils.getASTNodeType(parent.getParent()) +
    // parent.getParent().getParent().getNodeType());

  }

  void traceReturnStatement(PartIICCGNode parentPartIINode, String parentName, int level,
      final CompilationUnit cuLocal, ASTNode node, String configName, String containedMethod) {
    // Utils.printResult4("RETURN_STATEMENT");
    // Utils.printResult(level + " RETURN_STATEMENT " + node + " CALLED BY " + parentName + " AT " +
    // cuLocal.getLineNumber(node.getStartPosition()));
    Utils.printResult(level + " RETURN_STATEMENT " + node);
    // " called by " + parentName + "  :  " + configName + " in the return statement of method " +
    // containedMethod + " of class " +
    // savediCompilationUnit.getElementName());
  }

  void traceSuperConstructorInvocation(PartIICCGNode parentPartIINode, String parentName,
      int level, final CompilationUnit cuLocal, ASTNode node, String configName,
      String containedMethod, int idParam) {
    // Utils.printResult4("SUPER_CONSTRUCTOR_INVOCATION");
    SuperConstructorInvocation superClass = (SuperConstructorInvocation) node;
    // Utils.printResult(level + " SUPER_CONSTRUCTOR_INVOCATION " + node + " CALLED BY " +
    // parentName + " AT " + cuLocal.getLineNumber(node.getStartPosition()));
    Utils.printResult(level + " SUPER_CONSTRUCTOR_INVOCATION " + node);
    boolean isVoid = false;
    methodName = superClass.resolveConstructorBinding().getName();
    methodSignature = getMethodSignatureFromArguments(superClass.arguments());

    String qualifiedClassName =
        superClass.resolveConstructorBinding().getDeclaringClass().getQualifiedName();
    String className = superClass.resolveConstructorBinding().getDeclaringClass() + ".java";

    boolean foundClass = false;

    if (superClass.resolveConstructorBinding().getReturnType().getQualifiedName().equals("void")) {
      // &&
      // !"org.apache.commons.logging.Log".equals(superClass.getDeclaringClass().getQualifiedName()))
      // {
      isVoid = true;
    }

    // + parentName + "  :  " + configName + " is the " + idParam + "th parameter of " +
    // " is used for super constructor of class " + superClass.resolveConstructorBinding().getName()
    // + " from " + superClass.resolveConstructorBinding().getDeclaringClass().getQualifiedName()
    // + " with signature " + getMethodSignature(superClass.arguments()));

  }


  void traceConstructorInvocation(PartIICCGNode parentPartIINode, String parentName, int level,
      final CompilationUnit cuLocal, ASTNode node, final String configName,
      final String containedMethod, final int idParam) {
    // Utils.printResult4("CONSTRUCTOR_INVOCATION");
    ConstructorInvocation constructor = (ConstructorInvocation) node;
    methodSig = getMethodSignatureFromArguments(constructor.arguments());
    // Utils.printResult(level + " CONSTRUCTOR_INVOCATION " + node + " CALLED BY " + parentName +
    // " AT " + cuLocal.getLineNumber(node.getStartPosition()));
    Utils.printResult(level + " CONSTRUCTOR_INVOCATION " + node);
    // + configName + " is the " + idParam + "th parameter"
    // + " in a constructor of " + containedMethod + "(" + methodSig +
    // ") of class " + savediCompilationUnit.getElementName() + " with signature " +
    // getMethodSignature(constructor.arguments()));
    gLevel = level;
    if (cuLocal != null && cuLocal.getPackage() != null && cuLocal.getPackage().getName() != null) {
      cuLocal.accept(new ASTVisitor() {
        @Override
        public boolean visit(MethodDeclaration node) {
          if (node.getName().getFullyQualifiedName().equals(containedMethod)) {
            if (getMethodSignatureFromParameters(node.parameters()).equals(methodSig)) {
              Utils.printResult3(node.getName().getFullyQualifiedName());
              SingleVariableDeclaration singleVar =
                  (SingleVariableDeclaration) node.parameters().get(idParam);
              Utils.printResult3("We need to analyse parameter " + singleVar);
              traceVariableField(pIINode, node.toString(), gLevel + 1, cuLocal,
                  singleVar.getName(), configName, containedMethod);
            }
          }
          return true;
        }
      });
    }
  }

  // void setSavedICompilationUnit(ICompilationUnit savediCompilationUnit) {
  // this.savediCompilationUnit = savediCompilationUnit;
  // }
  //
  // void setInfoFortheFindMethodNameFunction(List<String> methodNames, List<Integer>
  // startLineofMethods, List<Integer> endLineofMethods) {
  //
  // }

  Set<String> tempMethodInvoList = new HashSet<String>();

  void traceParentStatement(PartIICCGNode parentPartIINode, String parentName, int level,
      final CompilationUnit cuLocal, int recursive, ASTNode node, String configName,
      String containedMethod) throws JavaModelException {
    // String typeStr = Utils.getASTNodeType(node.getParent());
    boolean isModified = false;
    ASTNode parent = node.getParent();
    PartIICCGNode pIINode =
        new PartIICCGNode(node.getNodeType(), new MethodInfo(containedMethod,
            partIICallerClassName, cuLocal.getLineNumber(node.getStartPosition())), level);
    if (recursive == 1) {
      // Utils.printResult2("RECURSIVELY ");
    }
    if ((recursive == NOTRECURSIVE || recursive == RECURSIVE)
        && parent.getNodeType() == ASTNode.ASSIGNMENT) { // get the variable/filed which is assigned
                                                         // by this assignment
      Assignment assign = (Assignment) node.getParent();
      Expression leftHandSide = assign.getLeftHandSide();
      String leftHandSideType = Utils.getExpressionRawType(leftHandSide);
      Utils.printResult3("ASSIGNMENT " + leftHandSideType);
      if (!visitedAssignments.contains(leftHandSide)) {
        // Utils.printResult("FOUNDIT " + visitedNode.size());
        visitedAssignments.add(leftHandSide);
        if (leftHandSideType.equals(Utils.FIELDACCESS)) {
          traceField(pIINode, node.toString(), level, cuLocal, leftHandSide, configName,
              containedMethod);
        } else {
          traceVariableField(pIINode, node.toString(), level, cuLocal, leftHandSide, configName, "");
        }

      }

      // return;
    } else if ((recursive == NOTRECURSIVE || recursive == RECURSIVE)
        && parent.getNodeType() == ASTNode.METHOD_INVOCATION) {
      MethodInvocation mInvocation = (MethodInvocation) parent;

      if (recursive == NOTRECURSIVE) {
        String methodName = mInvocation.getName().getFullyQualifiedName();
        tempMethodInvoList.add(mInvocation.resolveMethodBinding().getDeclaringClass()
            .getQualifiedName()
            + ":" + methodName);
        // System.out.println(methodName);
      }

      // Utils.printResult(mInvocation.toString()); ///print the whole statement
      // getMethodSignature(mInvocation.arguments());
      // List<Expression> arguments = mInvocation.arguments();
      // int idParam = 0;
      // for (int i = 0; i < arguments.size(); ++i) {
      // if (arguments.get(i).toString().contains(node.toString())) { //node.toString is the whole
      // statement, we need to check it to get the order of
      // //the configuration option in the invocation statement to do further analysis
      // // Utils.printResult("the " + i + "th element");
      // idParam = i;
      // break;
      // }
      // }
      if (recursive == NOTRECURSIVE)
        Utils.printResult4("METHOD_INVOCATION_HEHE");
      traceMethodInvocation(pIINode, node.toString(), level, 1, cuLocal, 0, mInvocation,
          configName, containedMethod, getParamID(mInvocation.arguments(), node.toString()));
      //

    } else if ((recursive == NOTRECURSIVE || recursive == RECURSIVE)
        && parent.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
      VariableDeclarationFragment varDec = (VariableDeclarationFragment) parent;
      Utils.printResult4("VARIABLE_DECLARATION_FRAGMENT level " + level);
      if (containedMethod.equals(savediCompilationUnit.getElementName().substring(0,
          savediCompilationUnit.getElementName().length() - 5))) { // this is a field
        countField++;
        // Utils.printResult("VARIABLE_DECLARATION_FRAGMENT " + configName +
        // " is assigned to field " + varDec.getName().getFullyQualifiedName() +
        // " of class " + savediCompilationUnit.getElementName());
        traceField(pIINode, node.toString(), level, cuLocal, varDec, configName, containedMethod);
      } else {
        countVariable++;
        // Utils.printResult("VARIABLE_DECLARATION_FRAGMENT " + configName +
        // " is assigned to variable " + varDec.getName().getFullyQualifiedName()
        // + " of method " + containedMethod + " of class " +
        // savediCompilationUnit.getElementName());
        traceVariableField(pIINode, node.toString(), level, cuLocal, varDec, configName,
            containedMethod);
      }
      if (recursive == NOTRECURSIVE) {
        String className = savediCompilationUnit.getElementName();
        int c = 0;
        if (scOptions.containsKey(methodName)) {
          c = scOptions.get(methodName) + 1;
        }
        scOptions.put(methodName, c);
        // Utils.printResult4("-------------");

        // Utils.printResult4("-------------");
      }
    } else

    if ((recursive == NOTRECURSIVE || recursive == RECURSIVE)
        && parent.getNodeType() == ASTNode.IF_STATEMENT) { // set recursive = 1 to recursively call
                                                           // this method
      if (recursive == NOTRECURSIVE)
        Utils.printResult4(level + " IF_STATEMENT_HEHE " + node + " CALLED BY " + parentName
            + " AT " + cuLocal.getLineNumber(node.getStartPosition()));
      typeOfParents[ASTNode.IF_STATEMENT] = 1;
      conditionalStmtUsage.add(new ConditionalStmtUsage(cuLocal.getJavaElement().getElementName(),
          "", configName));

      // + "  :  " + configName + " in a IfStatement of method " + containedMethod + " of class " +
      // savediCompilationUnit.getElementName());
      if (recursive == NOTRECURSIVE) {
        // Utils.printResult4("-------------");
      }
    } else

    if ((recursive == NOTRECURSIVE || recursive == RECURSIVE)
    // not sure. found Assignment and VariableDeclarationFragment
        && parent.getNodeType() == ASTNode.CONDITIONAL_EXPRESSION) {
      ConditionalExpression expr = (ConditionalExpression) node.getParent();
      // Utils.printResult(level + " CONDITIONAL_EXPRESSION " + node + " CALLED BY " + parentName +
      // " AT " + cuLocal.getLineNumber(node.getStartPosition()));
      Utils.printResult3(level + " CONDITIONAL_EXPRESSION " + node);
      // " called by " + parentName + "  :  " + configName + " in a " + "ConditionalExpression " +
      // Utils.getASTNodeType(node.getParent().getParent()));
      traceParentStatement(pIINode, node.toString(), level + 1, cuLocal, 1, parent, configName,
          containedMethod);
      if (recursive == NOTRECURSIVE) {
        Utils.printResult4("-------------");
      }
    } else

    // grandparent will be ReturnStatement, VariableDeclarationFragment or Assignment
    if ((recursive == NOTRECURSIVE || recursive == RECURSIVE)
        && parent.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
      if (recursive == NOTRECURSIVE)
        Utils.printResult4("CLASS_INSTANCE_CREATION");
      ClassInstanceCreation cInstance = (ClassInstanceCreation) parent;
      List<Expression> arguments = cInstance.arguments();
      //
      traceClassInstanceCreation(pIINode, node.toString(), level, cuLocal, cInstance, configName,
          containedMethod, getParamID(cInstance.arguments(), node.toString()));
      if (recursive == NOTRECURSIVE) {
        Utils.printResult3("-------------");
      }
    } else

    if ((recursive == NOTRECURSIVE || recursive == RECURSIVE)
        && parent.getNodeType() == ASTNode.INFIX_EXPRESSION) {

      InfixExpression expr = (InfixExpression) node.getParent();
      Operator opr = expr.getOperator();
      if (opr == Operator.DIVIDE || opr == Operator.EQUALS || opr == Operator.LEFT_SHIFT
          || opr == Operator.MINUS || opr == Operator.PLUS || opr == Operator.TIMES
          || opr == Operator.RIGHT_SHIFT_SIGNED)
        isModified = true;
      if (!visitedParentNodes.contains(parent)) {
        visitedParentNodes.add(parent);
      } else {
        Utils.printResult4("VISITED_INFIX_EXP");
        return;
      }
      // if (comparisonOperator.contains(expr.getOperator())) {
      // Utils.printResult(level + " INFIX_EXPRESSION " + node + " CALLED BY " + parentName + " AT "
      // + cuLocal.getLineNumber(node.getStartPosition()));
      if (recursive == NOTRECURSIVE)
        Utils.printResult4(level + " INFIX_EXPRESSION " + node);
      // " called by " + parentName + "  :  " + configName + " in a " + "InfixExpression " +
      // expr.getOperator() + " " +
      // Utils.getASTNodeType(node.getParent().getParent()) + " of method " + containedMethod);
      traceParentStatement(pIINode, node.toString(), level + 1, cuLocal, 1, parent, configName,
          containedMethod);
      // }
      if (recursive == NOTRECURSIVE) {
        Utils.printResult4("-------------");
      }
    } else

    if ((recursive == NOTRECURSIVE || recursive == RECURSIVE)
        && parent.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION) {
      isModified = true;
      // Utils.printResult(level + " PARENTHESIZED_EXPRESSION " + node + " CALLED BY " + parentName
      // + " AT " + cuLocal.getLineNumber(node.getStartPosition()));
      if (!visitedParentNodes.contains(parent)) {
        visitedParentNodes.add(parent);
      } else {
        Utils.printResult4("VISITED_PARENTHESIZED_EXPRESSION");
        return;
      }
      if (recursive == NOTRECURSIVE)
        Utils.printResult4(level + " PARENTHESIZED_EXPRESSION " + node);
      // " called by " + parentName + "  :  " + configName + " is in " +
      // Utils.getASTNodeType(parent.getParent()));
      traceParentStatement(pIINode, node.toString(), level + 1, cuLocal, 1, parent, configName,
          containedMethod);
      if (recursive == NOTRECURSIVE) {
        Utils.printResult4("-------------");
      }
    } else

    if ((recursive == NOTRECURSIVE || recursive == RECURSIVE)
        && parent.getNodeType() == ASTNode.PREFIX_EXPRESSION) {
      isModified = true;
      PrefixExpression expr = (PrefixExpression) parent;
      // Utils.printResult(level + " PREFIX_EXPRESSION " + node + " CALLED BY " + parentName +
      // " AT " + cuLocal.getLineNumber(node.getStartPosition()));
      if (!visitedParentNodes.contains(parent)) {
        visitedParentNodes.add(parent);
      } else {
        Utils.printResult4("VISITED_PREFIX_EXP");
        return;
      }
      Utils.printResult(level + " PREFIX_EXPRESSION " + node);
      // " called by " + parentName + "  :  " + configName + " in " +
      // Utils.getASTNodeType(parent.getParent()));
      traceParentStatement(pIINode, node.toString(), level + 1, cuLocal, 1, parent, configName,
          containedMethod);
      if (recursive == NOTRECURSIVE) {
        Utils.printResult3("-------------");
      }
    } else

    if ((recursive == NOTRECURSIVE || recursive == RECURSIVE)
        && parent.getNodeType() == ASTNode.RETURN_STATEMENT) {
      Utils.printResult3("RETURN_STATEMENT");
      ReturnStatement returnStmt = (ReturnStatement) parent;
      if (!visitedParentNodes.contains(parent)) {
        visitedParentNodes.add(parent);
      } else {
        Utils.printResult4("VISITED_RETURN_STMT");
        return;
      }
      traceReturnStatement(pIINode, node.toString(), level, cuLocal, returnStmt, configName,
          containedMethod);
      if (recursive == NOTRECURSIVE) {
        Utils.printResult3("-------------");
      }
    } else if ((recursive == NOTRECURSIVE || recursive == RECURSIVE)
        && parent.getNodeType() == ASTNode.CAST_EXPRESSION) {
      isModified = true;
      // Utils.printResult(level + " CAST_EXPRESSION " + node + " CALLED BY " + parentName + " AT "
      // + cuLocal.getLineNumber(node.getStartPosition()));
      if (!visitedParentNodes.contains(parent)) {
        visitedParentNodes.add(parent);
      } else {
        Utils.printResult4("VISITED_CAST_EXPRESSION");
        return;
      }
      Utils.printResult(level + " CAST_EXPRESSION " + node);
      // " called by " + parentName + "  :  " + Utils.getASTNodeType(parent.getParent()));
      traceParentStatement(pIINode, node.toString(), level + 1, cuLocal, 1, parent, configName,
          containedMethod);
      if (recursive == NOTRECURSIVE) {
        Utils.printResult3("-------------");
      }
    } else
    //
    if ((recursive == NOTRECURSIVE || recursive == RECURSIVE)
        && parent.getNodeType() == ASTNode.SUPER_CONSTRUCTOR_INVOCATION) {
      Utils.printResult4("SUPER_CONSTRUCTOR_INVOCATION");
      SuperConstructorInvocation superClass = (SuperConstructorInvocation) parent;
      traceSuperConstructorInvocation(pIINode, node.toString(), level, cuLocal, superClass,
          configName, containedMethod, getParamID(superClass.arguments(), node.toString()));
      if (recursive == NOTRECURSIVE) {
        Utils.printResult3("-------------");
      }
    } else

    if ((recursive == NOTRECURSIVE || recursive == RECURSIVE)
        && parent.getNodeType() == ASTNode.CONSTRUCTOR_INVOCATION) {
      Utils.printResult4("CONSTRUCTOR_INVOCATION");
      ConstructorInvocation constructor = (ConstructorInvocation) node.getParent();
      traceConstructorInvocation(pIINode, node.toString(), level, cuLocal, constructor, configName,
          containedMethod, getParamID(constructor.arguments(), node.toString()));
      if (recursive == NOTRECURSIVE) {
        Utils.printResult3("-------------");
      }
    }
    if ((recursive == NOTRECURSIVE || recursive == RECURSIVE)
        && parent.getNodeType() == ASTNode.FOR_STATEMENT) {
      Utils.printResult4("FORFOR");
    }
    if ((recursive == NOTRECURSIVE || recursive == RECURSIVE)
        && parent.getNodeType() == ASTNode.WHILE_STATEMENT) {
      Utils.printResult4("WHILEWHILE");
    }
    if ((recursive == NOTRECURSIVE || recursive == RECURSIVE)
        && parent.getNodeType() == ASTNode.SWITCH_STATEMENT) {
      Utils.printResult4("SWITCHSWITCH");
    }

    else {
      Utils.printResult4("CANNOTDETECT " + Utils.getASTNodeType(parent));
    }

    if (recursive == NOTRECURSIVE && isModified) {
      isModifiedGlobal = true;
      // Utils.printResult4("Modified");
    }
  }

  void updateClassHierarchy() {

  }

  public void performPhaseI(Map<String, MethodInfo> analysisPointsPartICCG,
      HashMap<String, MethodUsage> methodUsageContainer,
      HashMap<String, Set<SuperInterface>> subClassInfo) {
    try {
      updateClassHierarchy();

      // MethodUsage mU = methodUsageContainer.get("org.apache.hadoop.io.SequenceFile");
      // if (mU != null) {
      // System.err.println(mU.className
      // + " : "
      // + mU.methodUsageMap
      // .get("getDefaultCompressionType:org.apache.hadoop.conf.Configuration"));
      // }

      int countAnalysisPointIsAbstractInterfaces = 0;
      int countAnalysisPointImplementation = 0;
      System.out.println("buildPartI-CCG");
      int count = 0;
      for (String m : analysisPointsPartICCG.keySet()) {
        countStartingPoint = 0;
        // if (m.contains("get_count")) {
        MethodInfo mInfo = analysisPointsPartICCG.get(m);
        if (!mInfo.isTest()) {
          if (classHierarchyModel.containsKey(mInfo.className)) {
            ClassHierarchy cH = classHierarchyModel.get(mInfo.className);
            if (cH.isAbstractClass() || cH.isInterface()) {
              countAnalysisPointIsAbstractInterfaces++;
              // System.out.println("ABSTRACT INTERFACE " + mInfo.className + " : "
              // + mInfo.methodSignature);
            } else if (cH.isImplementationClass()) {
              countAnalysisPointImplementation++;
            }
          }
          // else {
          // System.out.println("Wierd " + m);
          // }
          // MethodUsage mUsage = methodUsageContainer.get(mInfo.className);
          this.partICCGWriter.write("BEGIN\n");
          this.partICCGWriter.write(count++ + " Method " + mInfo.methodSignature + " of class "
              + mInfo.className + " for ConfigurationOption " + mInfo.configName + "\n");
          int mode = 1; // 0: CCG for the whole program, 1: CCG for each option
          if (mode == 0) {
            PartICCGNode node = graph.getInternalNode(mInfo.toString());
            if (node == null) {
              node = new PartICCGNode(mInfo, 0);
              secondIndex.put(node.getId(), mInfo.toString());
              graph.addInternalNode(mInfo.toString(), node);
              // System.out.print(count++ + " Method " + node.getMethodInfo().methodSignature +
              // " of class " + mInfo.className + " for option " + mInfo.configName + " " +
              // node.getId()
              // + "\n");
              configNames.add(mInfo.configName);
              theConfigurationName = mInfo.configName;
              constructPartICCG(node, methodUsageContainer, subClassInfo, 0, -1);
            } else {
              this.partICCGWriter.write("Visited " + node.getId() + " : " + mInfo.toString()
                  + " of " + mInfo.className + "\n");
              // graph.clearVisitedSet();
              // graph.DFS(graph.getInternalNode(node.getId()), -1, this.partICCGWriter);
            }
          } else {
            PartICCGNode node = new PartICCGNode(mInfo, 0);
            secondIndex.put(node.getId(), mInfo.toString());
            graph.addInternalNode(mInfo.toString(), node);
            // System.out.print(count++ + " Method " + node.getMethodInfo().methodSignature +
            // " of class " + mInfo.className + " for option " + mInfo.configName + " " +
            // node.getId()
            // + "\n");
            configNames.add(mInfo.configName);
            theConfigurationName = mInfo.configName;
            // we clear the visited set because we want to build a CCG for each option
            visited.clear();
            constructPartICCG(node, methodUsageContainer, subClassInfo, 0, -1);
          }

          // if (mUsage != null) {
          // for (String calleeMethod : mUsage.methodUsageMap.keySet()) {
          //
          // // System.out.println(calleeMethod);
          // if (calleeMethod.equals(m)) {
          // DFS(mInfo, mUsage.methodUsageMap.get(calleeMethod), methodUsageContainer);
          // System.out.println("Found " + m);
          //
          // }
          // }
          // }
          // }
          this.partICCGWriter.write("END\n");
          this.partICCGWriter.write("countStartingPoint : " + countStartingPoint + "\n");
          this.partICCGWriter.write("===================\n\n");
          // }
        }
      }
      System.out.println("countAnalysisPointIsAbstractInterfaces : "
          + countAnalysisPointIsAbstractInterfaces);
      System.out.println("countAnalysisPointImplementation : " + countAnalysisPointImplementation);
      for (int i = 0; i < maxLevel; ++i) {
        System.out.println("countMUsage at level " + i + " : " + countMUsage[i] + " nonMUSage "
            + countNonMUsage[i] + " countNeighbor : " + countNeighbors[i] + " nonNeighbors "
            + countNonNeighbors[i] + " implementation : " + countImplementationHierarchyClasses[i]
            + " interface " + countInterfaceHierarchyClasses[i] + " abstract "
            + countAbstractHierarchyClasses[i] + " undefined " + countLibHierarchyClasses[i]
            + " noHierarchyInfo " + countNoHierarchyInfo[i]);
      }
      System.out.println("maxLevel : " + maxLevel);
      System.out.println("nbNeighbors : " + nbNeighbors);

      System.out.println("countNonNeighbors : " + countNonNeighbors);
      System.out.println("countNonNeighborImplementation : " + countNonNeighborImplementation);
      System.out.print("Number of Termianted Point " + realStartingPoints.size() + "\n");
      // graph.printGraph(methodUsageContainer);
      this.partICCGWriter.write("data file\n");
      this.partICCGWriter.write("Number of Termianted Point " + realStartingPoints.size() + "\n");
      this.partICCGWriter.write("nbOptionUsage " + nbOptionUsage + "\n");
      this.partICCGWriter.write("countCalledByMain " + countCalledByMain + "\n");
      this.partICCGWriter.write("countCalledByRun " + countCalledByRun + "\n");
      this.partICCGWriter.write("countCalledByMXBean " + countCalledByMXBean + "\n");
      this.partICCGWriter
          .write("countCalledByProtocolBuffer " + countCalledByProtocolBuffer + "\n");
      this.partICCGWriter.write("countCalledByCommandLine " + countCalledByCommandLine + "\n");
      this.partICCGWriter.write("countCalledByServlet " + countCalledByServlet + "\n");
      this.partICCGWriter.write("countCalledByTestFramework " + countCalledByTestFramework + "\n");
      this.partICCGWriter.write("countCalledByOtherProgram " + countCalledByOtherProgram + "\n");
      this.partICCGWriter.write("countCalledByMBean " + countCalledByMBean + "\n");

      this.partICCGWriter.write("-------------------\n");
      for (String key : optionStartingPointMap.keySet()) {
        if (key != null) {
          this.partICCGWriter.write(key);
          Set<String> pointSet = optionStartingPointMap.get(key);
          for (String key2 : pointSet) {
            this.partICCGWriter.write(key2 + "\n");
          }
          this.partICCGWriter.write("---------------\n");
        }
      }
      this.partICCGWriter.write("SIZE " + optionStartingPointMap.size() + "\n");
      this.partICCGWriter.write("SUPERSIZE " + configNames.size() + "\n");
      this.partICCGWriter.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public boolean checkExistanceOfAMethod(String methodName,
      HashMap<String, MethodUsage> methodUsageContainer) {
    for (String key : methodUsageContainer.keySet()) {
      MethodUsage m = methodUsageContainer.get(key);
      for (String key2 : m.methodUsageMap.keySet()) {
        // System.err.println("KEY2 " + key2);
        if (key2.split(":").length >= 1) {
          if (key2.split(":")[0].equals(methodName)) {
            System.out.println("FOUNDSAMECALLIN " + key);
            Set<MethodInfo> sameFunctionCall = m.methodUsageMap.get(key2);
            for (MethodInfo mInfo : sameFunctionCall) {
              // System.out.println(mInfo.className + " : " + mInfo.methodSignature);
              break;
            }
            return true;
          }
        }
      }
    }
    return false;
  }


  void updateOptionStartingPointMap(String startingPoint) {
    Set<String> pointSet;
    if (optionStartingPointMap.containsKey(theConfigurationName)) {
      pointSet = optionStartingPointMap.get(theConfigurationName);
    } else {
      pointSet = new HashSet<String>();
    }
    pointSet.add(startingPoint);
    optionStartingPointMap.put(theConfigurationName, pointSet);
  }

  boolean checkStartingPoints(MethodInfo mInfo, String realMethodName, int nId) throws IOException {
    String keyToCheck = mInfo.className + ":" + realMethodName;
    String fullKeyToCheck = mInfo.className + ":" + mInfo.methodSignature;
    this.partICCGWriter.write("checkStartingPoints " + nId + " " + fullKeyToCheck + "\n");
    boolean result = true;
    if (mxBeanClasses.contains(mInfo.className + "MXBean") || mInfo.className.contains("MBean")) {
      this.partICCGWriter.write("CalledByMXBean\n");
      this.partICCGWriter.write("---------\n");
      // System.err.println("MXBean");
      updateOptionStartingPointMap("CalledByMXBean");
      countCalledByMXBean++;
      return result;
    } else if (mInfo.methodSignature.contains("test") || (mInfo.className.contains("Test"))
        || (calledByTestFramework.contains(keyToCheck))) {
      this.partICCGWriter.write("CalledByTestFramework\n");
      this.partICCGWriter.write("---------\n");
      updateOptionStartingPointMap("CalledByTestFramework");
      countCalledByTestFramework++;
      return result;
    } else if (realMethodName.equals("main")) {
      updateOptionStartingPointMap("CalledByMain");
      this.partICCGWriter.write("CalledByMain\n");
      this.partICCGWriter.write("---------\n");
      countCalledByMain++;
      return result;
    } else if (calledByMain.contains(keyToCheck)) {
      updateOptionStartingPointMap("CalledByMain");
      this.partICCGWriter.write("CalledByMain\n");
      this.partICCGWriter.write("---------\n");
      countCalledByMain++;
      return result;
    } else if (mInfo.methodSignature.contains("protobuf.RpcController")) {
      this.partICCGWriter.write("CalledByProtocolBuffer\n");
      this.partICCGWriter.write("---------\n");
      updateOptionStartingPointMap("CalledByProtocolBuffer");
      countCalledByProtocolBuffer++;
      return result;
    } else if (realMethodName.equals("run")) {
      this.partICCGWriter.write("CalledByRun\n");
      this.partICCGWriter.write("---------\n");
      updateOptionStartingPointMap("CalledByRun");
      countCalledByRun++;
      return result;
    } else if (calledByMBean.contains(keyToCheck)) {
      this.partICCGWriter.write("CalledByMBean\n");
      this.partICCGWriter.write("---------\n");
      // System.err.println("MBEAN");
      updateOptionStartingPointMap("CalledByMBean");
      countCalledByMBean++;
      return result;
    } else if (calledByCommandLine.contains(keyToCheck)) {
      this.partICCGWriter.write("CalledByCommandLine\n");
      this.partICCGWriter.write("---------\n");
      updateOptionStartingPointMap("CalledByCommandLine");
      countCalledByCommandLine++;
      return result;

    } else if (unsureStartingPoints.contains(keyToCheck)) {
      // System.err.println(keyToCheck);
      this.partICCGWriter.write("Unsure");
      this.partICCGWriter.write("---------\n");
      // System.err.println("UNSURE");
      updateOptionStartingPointMap("Unsure");
      return result;
    } else if (calledByOtherProgram.contains(keyToCheck)) {
      // System.err.println("HEREWEGO");
      this.partICCGWriter.write("CalledByOtherProgram\n");
      this.partICCGWriter.write("---------\n");
      updateOptionStartingPointMap("CalledByOtherProgram");
      countCalledByOtherProgram++;
      return result;
    } else if (mInfo.className.contains("Servlet") || (mInfo.className.contains("_jsp"))) {
      this.partICCGWriter.write("CalledByServlet\n");
      this.partICCGWriter.write("---------\n");
      updateOptionStartingPointMap("CalledByServlet");
      countCalledByServlet++;
      return result;
    } else if (mInfo.methodSignature.contains("test") || (mInfo.className.contains("Test"))) {
      this.partICCGWriter.write("CalledByTestFramework");
      this.partICCGWriter.write("---------\n");
      updateOptionStartingPointMap("CalledByTestFramework");
      countCalledByTestFramework++;
      return result;
    } else if (mInfo.className.contains("Servlet") || (mInfo.className.contains("_jsp"))) {
      this.partICCGWriter.write("CalledByServlet");
      this.partICCGWriter.write("---------\n");
      updateOptionStartingPointMap("CalledByServlet");
      countCalledByServlet++;
      return result;
    } else if (calledByOtherModule.contains(keyToCheck)) {
      this.partICCGWriter.write("CalledByOtherModule\n");
      this.partICCGWriter.write("---------\n");
      System.out.println("CalledByOtherModule");
      return result;
      // TODO : need to check this later
    } else if (calledByOtherMethodIncludingParameter.contains(fullKeyToCheck)) {
      this.partICCGWriter.write("CalledByOtherIncludingParameter\n");
      this.partICCGWriter.write("---------\n");
      return result;
    } else if (depracatedMethod.contains(fullKeyToCheck)) {
      this.partICCGWriter.write("DeprecatedMethod\n");
      this.partICCGWriter.write("---------\n");
      return result;
    } else if (realMethodName.equals("serialize")) {// && mInfo.className.contains("Serializer")) {
      this.partICCGWriter.write("SERIALIZE\n");
      this.partICCGWriter.write("---------\n");
      return result;
    }
    return false;
  }

  // boolean checkSubClass(PartICCGNode node, MethodInfo mInfo, MethodUsage mUsage,
  // String realMethodName, boolean isStartingPoint, int level) throws IOException {
  // boolean result = true;
  // this.partICCGWriter.write("SubClass " + mInfo.className + " OF METHOD " + mInfo.methodSignature
  // + " FOR " + node.getId() + "\n");
  // // St
  // Set<SuperInterface> sInfSet = subClassInfo.get(mInfo.className);
  // boolean found = false;
  // for (SuperInterface sInf : sInfSet) {
  // if (sInf.overrideMethods.contains(realMethodName)) {
  // // System.out.println("WENEEDTOTRACK " + " method " + realMethodName + " OF " +
  // // sInf.className );
  // MethodUsage mSuperUsage = methodUsageContainer.get(sInf.className);
  // Set<MethodInfo> otherNeighbors = new HashSet<MethodInfo>();
  // if (mSuperUsage != null) {
  // for (String superMethodName : mSuperUsage.methodUsageMap.keySet()) {
  // if (superMethodName.contains(realMethodName)) {
  // found = true;
  // // ||
  // // (superMethodName.contains(realMethodName) && sInf.isParameter ==1 &&
  // // superMethodName.contains(sInf.parameterType))) {
  // this.partICCGWriter.write("WEFOUND " + superMethodName + " HAS " + realMethodName
  // + " FOR " + node.getId() + "\n");
  // otherNeighbors = mSuperUsage.methodUsageMap.get(superMethodName);
  // if (otherNeighbors != null) {
  // for (MethodInfo neighbor : otherNeighbors) {
  // PartICCGNode adjNode = graph.getInternalNode(neighbor.toString());
  // if (adjNode != null) {
  // node.addNeighborId(graph.getInternalNode(neighbor.toString()).getId());
  // // this.partICCGWriter.write("Add a new edge " + node.getId() + "-"
  // // + graph.getInternalNode(neighbor.toString()).getId() + "\n");
  // isStartingPoint = false;
  // } else {
  // adjNode = new PartICCGNode(neighbor, level);
  // graph.addInternalNode(neighbor.toString(), adjNode);
  // node.addNeighborId(adjNode.getId());
  // // this.partICCGWriter.write("Add a new vertex " + adjNode.getId()
  // // + " and an edge " + node.getId() + "-" + adjNode.getId() + "\n");
  // isStartingPoint = false;
  // }
  // if (level == 0) {
  // nbOptionUsage++;
  // }
  // }
  // if (isStartingPoint) {
  // if (mxBeanClasses.contains(mInfo.className + "MXBean")
  // || mInfo.className.contains("MBean")) {
  // this.partICCGWriter.write("CalledByMXBean\n");
  // updateOptionStartingPointMap("CalledByMXBean");
  // countCalledByMXBean++;
  // } else {
  // updateOptionStartingPointMap("Starting Point");
  // countStartingPoint++;
  // System.out.println("Starting Point - NoMoreNeighborsData");
  // }
  // } else {
  // graph.updateInternalNode(mInfo.toString(), node);
  // for (MethodInfo neighbor : otherNeighbors) {
  // constructPartICCG(graph.getInternalNode(neighbor.toString()),
  // methodUsageContainer, subClassInfo, 1, node.getId());
  // }
  // }
  // }
  // }
  // }
  //
  // } else {
  // // System.out.println("CANTFINDCLASS");
  // }
  // } else {
  // // System.out.println("CANTFIND");
  // }
  // }
  // if (!found) {
  // // if (checkStandardLib())
  // {
  // for (String potentialMethod : mUsage.methodUsageMap.keySet()) {
  // if (potentialMethod.split(":")[0].equals(realMethodName)) {
  // if (mInfo.methodSignature.split("\t").length == potentialMethod.split("\t").length) {
  // System.out.println("SAMEMETHODDIFFERENTPARAMETERS"
  // + mInfo.methodSignature.split("\t").length);
  // for (String t1 : mInfo.methodSignature.split("\t")) {
  // System.out.println(t1 + " =>  ");
  // }
  // for (String t1 : potentialMethod.split("\t")) {
  // System.out.println(t1 + " <= ");
  // }
  // // System.out.println(realMethodName.split("\\*"));
  // // System.out.println(potentialMethod.split("\\*"));
  // found = true;
  // }
  // }
  // }
  // }
  //
  // }
  //
  // if (!found) {
  // if (!checkExistanceOfAMethod(realMethodName, methodUsageContainer)) {
  // System.out.println("CalledByOtherProgram22");
  // updateOptionStartingPointMap("CalledByOtherProgram22");
  // countCalledByOtherProgram++;
  // // return ;
  // }
  // if (realMethodName.equals("run")) {
  // System.out.println("CalledByRun");
  // updateOptionStartingPointMap("CalledByRun");
  // countCalledByRun++;
  // found = true;
  // // return;
  // } else if (realMethodName.equals("exec")) {
  // updateOptionStartingPointMap("CalledByRun");
  // System.out.println("CalledByRun");
  // countCalledByRun++;
  // found = true;
  // // return;
  // } else {
  // updateOptionStartingPointMap("Entry Point");
  // System.out.println("Entry Point - SuperClassNotFound " + " FOR " + node.getId());
  // }
  // }
  // return true;
  // }

  void addToGraph(PartICCGNode node, MethodInfo neighbor, int level) throws IOException {
    PartICCGNode adjNode = graph.getInternalNode(neighbor.toString());
    if (adjNode != null) {
      node.addNeighborId(graph.getInternalNode(neighbor.toString()).getId());
      // this.partICCGWriter.write("Add a new edge " + node.getId() + "-"
      // + graph.getInternalNode(neighbor.toString()).getId() + "\n");
      isStartingPoint = false;
    } else {
      adjNode = new PartICCGNode(neighbor, level);
      graph.addInternalNode(neighbor.toString(), adjNode);
      node.addNeighborId(adjNode.getId());
      // this.partICCGWriter.write("Add a new vertex " + adjNode.getId() + " and an edge "
      // + node.getId() + "-" + adjNode.getId() + "\n");
      isStartingPoint = false;
    }
    if (level == 0) {
      nbOptionUsage++;
    }
  }

  void addNewNode(PartICCGNode node, MethodInfo mInfo, String methodSig, String className, int level)
      throws IOException {
    MethodInfo newMInfo = new MethodInfo(methodSig, className, -1);
    isStartingPoint = true;
    addToGraph(node, newMInfo, level);
    if (isStartingPoint) {
      if (checkStartingPoints(mInfo, "", node.getId())) {
        countStartingPoint++;
        updateOptionStartingPointMap("Starting Point");
        // this.partICCGWriter.write("Starting Point - No More Neighbors Data\n");
        realStartingPoints.add(mInfo.toString());
      }
    } else {
      graph.updateInternalNode(mInfo.toString(), node);
      this.partICCGWriter.write("Traversing " + newMInfo.toString() + " " + newMInfo.lineNumber
          + "\n");
      // System.out.println("Traversing " + threadClass.toString());
      constructPartICCG(graph.getInternalNode(newMInfo.toString()), methodUsageContainer,
          subClassInfo, level + 1, node.getId());
    }
  }

  /**
   * 
   * @param node
   * @param methodUsageContainer
   * @param subClassInfo
   * @param level
   */
  final int MAXLEVEL = 100;
  boolean isStartingPoint = true;
  int nbNodes = 0;
  int[] countMUsage = new int[MAXLEVEL];
  int[] countNonMUsage = new int[MAXLEVEL];
  int[] countNeighbors = new int[MAXLEVEL];
  int[] countAbstractHierarchyClasses = new int[MAXLEVEL];
  int[] countImplementationHierarchyClasses = new int[MAXLEVEL];
  int[] countInterfaceHierarchyClasses = new int[MAXLEVEL];
  int[] countLibHierarchyClasses = new int[MAXLEVEL];
  int[] countNoHierarchyInfo = new int[MAXLEVEL];
  int[] countNonNeighbors = new int[MAXLEVEL];
  int countNonNeighbor = 0;
  int countNonNeighborImplementation = 0;
  int maxLevel = 0;
  int nbNeighbors = 0;
  int nbVisited = 0;
  int countCallCheckSameMethod = 0;

  public void constructPartICCG(PartICCGNode node,
      HashMap<String, MethodUsage> methodUsageContainer,
      HashMap<String, Set<SuperInterface>> subClassInfo, int level, int parentID) {
    try {
      if (level > maxLevel) {
        maxLevel = level;
      }
      MethodInfo mInfo = node.getMethodInfo();
      if (visited.contains(mInfo.toString())) {
        this.partICCGWriter.write("Visited " + parentID + "-" + node.getId() + " : "
            + mInfo.toString() + " of " + mInfo.className + "\n");
        this.partICCGWriter.write("---------\n");
        nbVisited++;
        // graph.clearVisitedSet();
        // graph.DFS(graph.getInternalNode(node.getId()), -1, this.partICCGWriter);
        return;
      }
      nbNodes++;
      this.partICCGWriter.write("Visiting " + parentID + "-" + node.getId() + " : "
          + mInfo.toString() + " of " + mInfo.className + " " + mInfo.lineNumber + "\n");
      visited.add(mInfo.toString());
      MethodUsage mUsage = methodUsageContainer.get(mInfo.className);

      Set<MethodInfo> neighbors;// = new HashSet<MethodInfo>();

      String realMethodName = mInfo.methodSignature.split(":")[0].trim(); // without parameter types

      ClassHierarchy cH = null;

      if (classHierarchyModel.containsKey(mInfo.className)) {
        // this.partICCGWriter.write("HAS CLASS HIERARCHY\n");
        cH = classHierarchyModel.get(mInfo.className);
      }
      if (mUsage != null) {
        // if (level == 5) {
        // System.out.println("AT_LEVEL 1 " + mInfo.methodSignature + " : " + realMethodName);
        countMUsage[level]++;
        // }

        neighbors = mUsage.methodUsageMap.get(mInfo.methodSignature);

        if (neighbors != null) {
          if (!cH.isUndefined()) {
            String classType = "dontknowtype";
            if (cH != null) {
              if (cH.isImplementationClass()) {
                classType = "implementation";
              } else if (cH.isInterface()) {
                classType = "interface";
              } else if (cH.isAbstractClass()) {
                classType = "abstract";
              } else if (cH.isUndefined()) {
                classType = "undefined";
              }
            }
            this.partICCGWriter.write(classType + " " + node.getId() + " " + mInfo.className + ":"
                + mInfo.methodSignature + " " + mInfo.lineNumber + " HAS NEIGHBORS\n");
            // if (level == 1) {
            // System.out.println("AT_LEVEL 1");
            countNeighbors[level]++;
            // }

            nbNeighbors += neighbors.size();
            isStartingPoint = true;
            graph.updateInternalNode(mInfo.toString(), node);
            for (MethodInfo neighbor : neighbors) {
              addToGraph(node, neighbor, level);
            }
            if (isStartingPoint) {
              if (checkStartingPoints(mInfo, "", node.getId())) {
                countStartingPoint++;
                updateOptionStartingPointMap("Starting Point");
                // this.partICCGWriter.write("Starting Point - No More Neighbors Data\n");
                realStartingPoints.add(mInfo.toString());
              }
            } else {
              // graph.updateInternalNode(mInfo.toString(), node);
              for (MethodInfo neighbor : neighbors) {
                // System.out.println("neighbor " + neighbor.toString());
                constructPartICCG(graph.getInternalNode(neighbor.toString()), methodUsageContainer,
                    subClassInfo, level + 1, node.getId());
              }
            }
          } else {
            if (!checkStartingPoints(mInfo, realMethodName, node.getId())) {
              this.partICCGWriter.write("undefined - cannot detect");
            }
          }
        } else { // this node does not have neighbors
          // this.partICCGWriter.write(mInfo.methodSignature + " DOES NOT HAVE NEIGHBORS\n");
          if (cH != null) {
            // this.partICCGWriter.write("HAS CLASS HIERARCHY\n");
            boolean reached = false;

            if (cH.isImplementationClass()) {
              // this.partICCGWriter.write("Implementation\n");
              if (cH.hasBaseClass()) {
                // this.partICCGWriter.write("HasBaseClass\n");
                // JDT can help to detect overried method
                if (mInfo.methodSignature.equals("run:")) { // inherits a thread
                  if (cH.inheritClass("java.lang.Thread") || cH.inheritClass("java.lang.Runnable")) {
                    this.partICCGWriter.write(node.getId()
                        + " hasBaseClass - THREAD IMPLEMENTATION " + mInfo.className + " METHOD: "
                        + mInfo.methodSignature + " at " + mInfo.lineNumber + " has simplename "
                        + mInfo.getSimpleClassName() + "\n");
                    // System.out.println("THREAD IMPLEMENTATION " + mInfo.className + " METHOD: "
                    // + mInfo.methodSignature + " at " + mInfo.lineNumber + " has simplename "
                    // + mInfo.getSimpleClassName() + "\n");
                    if (mUsage.methodUsageMap.containsKey(mInfo.getSimpleClassName() + ":")) { //
                      // System.out.println("THREAD INSTANCE " + mInfo.className);
                      // addNewNode(node, mInfo, mInfo.getSimpleClassName() + ":", mInfo.className,
                      // level); // mInfo.getSimpleClassName()+":" is method signature
                    }
                    reached = true;
                  } else {
                    this.partICCGWriter.write(node.getId() + " hasBaseClass - RUNRUN "
                        + mInfo.toString() + " \n");
                    // System.out.println("RUNRUN " + mInfo.toString());
                  } // JDT cannot help to detect override method
                }
                if (mUsage.checkSameMethodName(mInfo.methodSignature, classToInterface)) {
                  countCallCheckSameMethod++;
                  // System.err.println("WTF");
                  String sMethod =
                      mUsage.getSameMethodName(mInfo.methodSignature, classToInterface);

                  if (sMethod != null) {
                    this.partICCGWriter.write(node.getId()
                        + " hasBaseClass - METHOD WITH HIERARCHY PARAMETERS " + mInfo.className
                        + " METHOD: " + mInfo.methodSignature + " FROM " + sMethod + " at "
                        + mInfo.lineNumber);
                    // System.err.println("METHOD WITH HIERARCHY PARAMETERS " + mInfo.className
                    // + " METHOD: " + mInfo.methodSignature + "\nFROM " + sMethod + " at "
                    // + mInfo.lineNumber);
                    reached = true;
                    addNewNode(node, mInfo, sMethod, mInfo.className, level);
                  } else {
                    this.partICCGWriter.write(node.getId() + " hasBaseClass - NOSAMEMETHODNAME "
                        + mInfo.toString() + "\n");
                    // System.err.println("NOSAMEMETHODNAME " + mInfo.toString());
                  }
                }
                if (checkStartingPoints(mInfo, realMethodName, node.getId())) {
                  countStartingPoint++;
                  // System.out.println("DETERMINED POINT " + mInfo.className + " : "
                  // + mInfo.methodSignature + " at " + mInfo.lineNumber);
                  // this.partICCGWriter.write("DETERMINED POINT " + mInfo.className + " : "
                  // + mInfo.methodSignature + " at " + mInfo.lineNumber + "\n");
                  // this.partICCGWriter.write("---------\n");
                  reached = true;
                }

                // System.err.println("HERE2");
                if (reached) {
                  // this.partICCGWriter.write("Reached\n");
                  countNeighbors[level]++;
                } else {
                  String bClass = cH.overrideMethod(mInfo.methodSignature);
                  if (bClass != null) { // overried a method of the base class
                    this.partICCGWriter.write(node.getId() + " HASBASECLASS - overrideMethod  "
                        + mInfo.className + " METHOD: " + mInfo.methodSignature + " FROM "
                        + bClass.split("::")[0] + " METHOD " + bClass.split("::")[1] + "\n");
                    // System.err.println("hasBaseClass  " + mInfo.className + " METHOD: "
                    // + mInfo.methodSignature + " FROM " + bClass.split("::")[0] + " METHOD "
                    // + bClass.split("::")[1]);
                    countImplementationHierarchyClasses[level]++;
                    addNewNode(node, mInfo, bClass.split("::")[1], bClass.split("::")[0], level);
                    reached = true;
                  }
                }
                if (!reached) {
                  countNonNeighbors[level]++;
                  countNonNeighbor++;
                  // System.out.println("no information about usage  " + mInfo.className +
                  // " METHOD: "
                  // + mInfo.methodSignature + " at " + mInfo.lineNumber);
                  this.partICCGWriter.write(node.getId()
                      + " hasBaseClass - no neighbor - cannot detect  " + mInfo.className + ":"
                      + mInfo.methodSignature + " at " + mInfo.lineNumber + " path " + mInfo.path
                      + "\n");
                  this.partICCGWriter.write("---------\n");
                }
              } else if (!cH.hasBaseClass() && cH.hasSubClass()) {
                this.partICCGWriter
                    .write("No Base Clas - Has Sub Class " + mInfo.toString() + "\n");
                // System.out.println("No Base Clas - Has Sub Class " + mInfo.toString());
                // if (cH.isOverriedMethod(mInfo.methodSignature))
                // System.err.println(mInfo.className + " : " + mInfo.methodSignature + " "
                // + cH.subClassesToString());
              } else if (!cH.hasBaseClass() && !cH.hasSubClass()) {
                if (!checkStartingPoints(mInfo, realMethodName, node.getId())) {
                  if (mUsage.checkSameMethodName(mInfo.methodSignature, classToInterface)) {
                    countCallCheckSameMethod++;
                    String sMethod =
                        mUsage.getSameMethodName(mInfo.methodSignature, classToInterface);
                    if (sMethod != null) {
                      // System.out.println("METHOD WITH HIERARCHY PARAMETERS " + mInfo.className
                      // + " METHOD: " + mInfo.methodSignature + "\nFROM " + sMethod + " at "
                      // + mInfo.lineNumber);
                      this.partICCGWriter.write(node.getId()
                          + " no base - no sub - METHOD WITH HIERARCHY PARAMETERS "
                          + mInfo.className + " METHOD: " + mInfo.methodSignature + "\nFROM "
                          + sMethod + " at " + mInfo.lineNumber + "\n");
                      reached = true;
                      addNewNode(node, mInfo, sMethod, mInfo.className, level);
                    } else {
                      this.partICCGWriter.write(node.getId()
                          + " no base - no sub - NOSAMEMETHODNAME " + mInfo.toString() + "\n");
                      // System.out.println("NOSAMEMETHODNAME " + mInfo.toString());
                    }
                  } else {
                    this.partICCGWriter.write(node.getId() + " no base - no sub - no neighbor "
                        + mInfo.toString() + " at " + mInfo.lineNumber + "\n");
                    // System.out.println("NO BASE - NO SUB " + mInfo.toString() + " at "
                    // + mInfo.lineNumber);
                  }
                } else {
                  countStartingPoint++;
                }
              }
            } else if (cH.isInterface() || cH.isAbstractClass()) { // Abstracr or Interface
              // this.partICCGWriter.write("Abstracr or Interface");

              if (mUsage.checkSameMethodName(mInfo.methodSignature, classToInterface)) {
                countCallCheckSameMethod++;
                String sMethod = mUsage.getSameMethodName(mInfo.methodSignature, classToInterface);
                if (sMethod != null) {
                  // System.out.println("Abstracr or Interface METHOD WITH HIERARCHY PARAMETERS "
                  // + mInfo.className + " METHOD: " + mInfo.methodSignature + "\nFROM " + sMethod
                  // + " at " + mInfo.lineNumber);
                  this.partICCGWriter.write(node.getId()
                      + " Abstract or Interface  - METHOD WITH HIERARCHY PARAMETERS "
                      + mInfo.className + " METHOD: " + mInfo.methodSignature + "\nFROM " + sMethod
                      + " at " + mInfo.lineNumber + "\n");
                  reached = true;
                  addNewNode(node, mInfo, sMethod, mInfo.className, level);
                } else {
                  this.partICCGWriter.write(node.getId()
                      + " Abstract or Interface - NOSAMEMETHODNAME \n");
                }
              }
              if (!reached) {
                PartICCGNode p1Node = this.graph.getInternalNode(parentID);
                if (classHierarchyModel.containsKey(mInfo.className)) {
                  // this.partICCGWriter.write("HAS CLASS HIERARCHY\n");
                  ClassHierarchy parentCH =
                      classHierarchyModel.get(p1Node.getMethodInfo().className);
                }//
                String bClass = cH.overrideMethod(mInfo.methodSignature);
                if (bClass != null) { // overried a method of the base class
                  this.partICCGWriter.write(node.getId()
                      + " Abstract or Interface - overrideMethod  " + mInfo.className + " METHOD: "
                      + mInfo.methodSignature + " FROM " + bClass.split("::")[0] + " METHOD "
                      + bClass.split("::")[1] + "\n");
                  // System.out.println("Abstracr or Interface hasBaseClass  " + mInfo.className
                  // + " METHOD: " + mInfo.methodSignature + " FROM " + bClass.split("::")[0]
                  // + " METHOD " + bClass.split("::")[1]);
                  countImplementationHierarchyClasses[level]++;
                  addNewNode(node, mInfo, bClass.split("::")[1], bClass.split("::")[0], level);
                  reached = true;
                }
              }
              if (!reached) {
                this.partICCGWriter.write(node.getId() + " Abstract or Interface - no neighbor "
                    + mInfo.toString() + " at " + mInfo.lineNumber + "\n");
                this.partICCGWriter.write("---------\n");
              }
            }


            // if (cH.isImplementationClass() && cH.hasSubClass()) {
            // if ( cH.isOverriedMethod(mInfo.methodSignature)) {
            // System.out.println("hasSubClass  " + mInfo.className + " METHOD: " +
            // mInfo.methodSignature);
            // countImplementationHierarchyClasses[level]++;
            // }
            // }

            // if (cH.isImplementationClass()) {
            // countImplementationHierarchyClasses[level]++;
            // }
            if (cH.isAbstractClass()) {
              countAbstractHierarchyClasses[level]++;
              // System.out.println("No Neighbors Abstract " + mInfo.className + " : " +
              // mInfo.methodSignature);
            }
            if (cH.isInterface()) {
              countInterfaceHierarchyClasses[level]++;
              // System.out.println("No Neighbors Interface " + mInfo.className + " : " +
              // mInfo.methodSignature);
            }
            if (cH.isUndefined()) {
              countLibHierarchyClasses[level]++;
              // System.out.println("No Neighbors Undefined " + mInfo.className + " : " +
              // mInfo.methodSignature);
            }
          } else {
            countNoHierarchyInfo[level]++;
            this.partICCGWriter.write(node.getId() + " no neighbor - noHierarchyInfo - "
                + mInfo.toString() + "\n");
          }
          /*
           * this.partICCGWriter.write("No neighbors of \n"); // if (!isStartingPoint) { if
           * (reflectionClasses.contains(mInfo.className)) { //
           * System.out.println("ANALYZEING REFLECTION"); neighbors =
           * mUsage.methodUsageMap.get(realMethodName);
           * this.partICCGWriter.write(mInfo.methodSignature.split(":")[0].trim() + "\n"); if
           * (neighbors != null) { for (MethodInfo neighbor : neighbors) { addToGraph(node,
           * neighbor, level); } if (!isStartingPoint) { graph.updateInternalNode(mInfo.toString(),
           * node); for (MethodInfo neighbor : neighbors) {
           * constructPartICCG(graph.getInternalNode(neighbor.toString()), methodUsageContainer,
           * subClassInfo, 1); } } } }
           * 
           * 
           * if (checkStartingPoints(mInfo, realMethodName)) { return; } else if
           * (subClassInfo.containsKey(mInfo.className)) { checkSubClass(node, mInfo, mUsage,
           * realMethodName, isStartingPoint, level);
           * 
           * 
           * } else { if (!checkExistanceOfAMethod(realMethodName, methodUsageContainer)) {
           * System.out.println("CalledByOtherProgram22");
           * updateOptionStartingPointMap("CalledByOtherProgram22"); countCalledByOtherProgram++;
           * return; } else { updateOptionStartingPointMap("Entry Point"); countStartingPoint++;
           * System.out.println("Entry Point - NoUsageData " + " FOR " + node.getId()); //
           * realStartingPoints.add(mInfo.toString()); } } // }
           */
        }
      } else { // no method usage information
        // this.partICCGWriter.write(mInfo.methodSignature + " DOES NOT HAVE METHOD USAGE\n");
        boolean reached = false;
        if (checkStartingPoints(mInfo, realMethodName, node.getId())) {
          countStartingPoint++;
          // System.out.println("DETERMINED POINT " + mInfo.className + " : "
          // + mInfo.methodSignature + " at " + mInfo.lineNumber);
          // this.partICCGWriter.write("DETERMINED POINT " + mInfo.className + " : "
          // + mInfo.methodSignature + " at " + mInfo.lineNumber + "\n");
          // this.partICCGWriter.write("---------\n");
          reached = true;
        }
        if (!reached) {
          if (cH != null) {
            String bClass = cH.overrideMethod(mInfo.methodSignature);
            if (bClass != null) { // overried a method of the base class
              this.partICCGWriter.write(node.getId() + " no mUsage - overrideMethod "
                  + mInfo.className + " METHOD: " + mInfo.methodSignature + " FROM "
                  + bClass.split("::")[0] + " METHOD " + bClass.split("::")[1] + "\n");
              countImplementationHierarchyClasses[level]++;
              addNewNode(node, mInfo, bClass.split("::")[1], bClass.split("::")[0], level);
              reached = true;
            }
          }
        }
        if (!reached) {
          countNonMUsage[level]++;
          // System.out.println("No mUsage " + mInfo.toString() + " at " + mInfo.lineNumber);
          this.partICCGWriter.write(node.getId() + " no mUsage - " + mInfo.toString() + " at "
              + mInfo.lineNumber + "\n");
          this.partICCGWriter.write("---------\n");
        }
        /*
         * System.out.println("NOINFOOFCLASS " + mInfo.className); if (!checkStartingPoints(mInfo,
         * realMethodName)) { if (subClassInfo.containsKey(mInfo.className)) { checkSubClass(node,
         * mInfo, mUsage, realMethodName, isStartingPoint, level); } else { if
         * (!checkExistanceOfAMethod(realMethodName, methodUsageContainer)) {
         * System.out.println("CalledByOtherProgram22");
         * updateOptionStartingPointMap("CalledByOtherProgram22"); countCalledByOtherProgram++;
         * return; } else { countStartingPoint++; System.out.println("Starting Point - NoClassData"
         * + " FOR " + node.getId()); updateOptionStartingPointMap("Starting Point"); //
         * realStartingPoints.add(mInfo.className); } } }
         */
      }
    } catch (Exception e) {
    }
  }

  void constructMultiLevelClassHierarchyForExternalLib(String baseClassName,
      ClassHierarchy originalcH, String className, String newClassName) {
    if (methodUsageContainer.containsKey(baseClassName)) {
      MethodUsage mUsage = methodUsageContainer.get(baseClassName);
      for (String mSig : mUsage.methodUsageMap.keySet()) { // every method of the base class
        // System.err.println(mSig);
        MethodDeclaration methodDec = originalcH.isOverride(mSig);
        if (methodDec != null) {
          String methodName = methodDec.getName().getFullyQualifiedName();
          // System.out.println("EXTERNAL CLASS " + baseClassName + " FROM " + newClassName);
          String methodSig =
              methodName + ":" + getMethodSignatureFromParameters(methodDec.parameters());
          // System.out.println("METHODSIG " + methodSig + " OF " + className);
          String baseMethodSig = baseClassName + "::" + mSig;
          originalcH.updateBaseMethod(methodSig, baseMethodSig);
          // originalcH.addBaseClass(orgBaseClassName);
          // baseCH.addSubClass(className);
        }
      }
    }
  }

  void constructMultiLevelClassHierarchy(HashSet<String> baseClassNames, String className,
      String newClassName, ClassHierarchy originalcH, ClassHierarchy cH, int level) {
    // System.out.println("LEVEL " + level);
    // System.out.println("newClassName : " + newClassName);
    // for (String className : classHierarchyModel.keySet()) {
    // ClassHierarchy cH = classHierarchyModel.get(className);
    // if (className.contains("DebuggableThreadPoolExecutor")) {

    if (cH.hasBaseClass()) {
      if (level >= 1) {

        for (String orgBaseClassName : cH.getBaseClasses()) {
          // System.out.println("orgBaseClassName : " + orgBaseClassName);
          baseClassNames.add(orgBaseClassName);
        }
      }
      // System.out.println("SIZE " + baseClassNames.size());
      for (String orgBaseClassName : cH.getBaseClasses()) {
        String baseClassName = orgBaseClassName;

        if (orgBaseClassName.contains("<")) {
          baseClassName = orgBaseClassName.substring(0, orgBaseClassName.indexOf("<"));
          // System.out.println("<< " + baseClassName + " : " + orgBaseClassName);
        }
        // System.out.println("CHECKING " + baseClassName);
        // if this is a class in the being parsed software
        if (classHierarchyModel.containsKey(baseClassName)) {
          ClassHierarchy baseCH = classHierarchyModel.get(baseClassName);

          if (baseCH.getMethodDecSet() != null) {
            // System.out.println("BASE_CLASS " + baseClassName);
            for (MethodDeclaration baseMethodDec : baseCH.getMethodDecSet()) {
              String methodName = baseMethodDec.getName().getFullyQualifiedName();
              // System.out.println("methodName " + methodName);
              // System.out.println(getMethodSignatureFromParameters(baseMethodDec.parameters()));
              MethodDeclaration methodDec =
                  originalcH.isOverride(methodName, baseMethodDec.resolveBinding(),
                      baseMethodDec.parameters(), classToInterface);
              if (methodDec != null) {

                String methodSig =
                    methodName + ":" + getMethodSignatureFromParameters(methodDec.parameters());
                String baseMethodSig =
                    baseClassName + "::" + methodName + ":"
                        + getMethodSignatureFromParameters(baseMethodDec.parameters());
                originalcH.updateBaseMethod(methodSig, baseMethodSig);
                // originalcH.addBaseClass(orgBaseClassName);
                // baseCH.addSubClass(className);
                baseCH.updateSubMethod(baseMethodSig, methodSig);
                classHierarchyModel.put(baseClassName, baseCH);
                // if (level >= 1) {
                // System.out.println("MultiLevel " + methodName + " OF " + className + " AND " +
                // baseClassName);
                // }
                // if (baseCH.isInterface()) {
                // countOverriedFromInterface++;
                // this.classHierarchytmpFileWriter.write(className + " == " + methodSig + " == " +
                // baseMethodSig + "\n");
                // }
              }
            }
          } else { // class of external lib does not have method declaration set
            // System.out.println("BASE CLASS OF AN EXTERNAL LIB SO THERE IS NO METHOD DECLARATIONS ");
            constructMultiLevelClassHierarchyForExternalLib(baseClassName, originalcH, className,
                newClassName);
          }
          constructMultiLevelClassHierarchy(baseClassNames, className, orgBaseClassName,
              originalcH, baseCH, level + 1);
        } else {
          // TODO: we need to update multilevel class hierarchy after calling function
          // getMethodUsageInformation. For example, org.apache.cassandra.hadoop.BulkRecordWriter
          // METHOD: write:java.nio.ByteBuffer java.util.List
          // originalcH.addBaseClass(orgBaseClassName);
          //
          constructMultiLevelClassHierarchyForExternalLib(baseClassName, originalcH, className,
              newClassName);
        }
      }

      classHierarchyModel.put(className, originalcH);
    }
    // }
    // }
  }

  int countAbstractClass = 0;
  int countInnerAbstractClass = 0;
  int countInterface = 0;
  int countInnerInterface = 0;
  int countImplementationClass = 0;
  int countInnerClass = 0;
  int countNbClasses = 0;
  int nbTotalClasses = 0;
  int countNoInheritance = 0;
  int countMethods = 0;
  int countImplementationMethods = 0;
  int countAbstractMethods = 0;
  int countInterfaceMethods = 0;
  CompilationUnit cuConstructClassHierarchy;

  /**
   * 
   * @throws JavaModelException
   * @throws IOException
   */
  void constructClassHierarchy() throws JavaModelException, IOException {
    for (IPackageFragment mypackage : globalPackages) {
      if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
        // System.err.println("Package " + mypackage.getElementName());

        for (ICompilationUnit iCompilationUnit : mypackage.getCompilationUnits()) {
          // System.out.println(iCompilationUnit.getPath().toString());

          if (iCompilationUnit.getPath().toString().contains(global_source_path)
              && !iCompilationUnit.getElementName().contains("thrift/gen-java")) {
            // &&
            // iCompilationUnit.getElementName().equals("DatacenterAwareRequestCoordinator.java")) {
            // && !iCompilationUnit.getElementName().equals(propReadFileName) ) { //analyze the
            // class which reads options from the property file. For example,
            // DatabaseDescriptor.java of Cassandra
            // System.out.println("Path " + iCompilationUnit.getPath().toString());
            callerClassName =
                mypackage.getElementName() + "."
                    + iCompilationUnit.getElementName().replace(".java", "");
            ASTParser parser = ASTParser.newParser(AST.JLS8);
            parser.setSource(iCompilationUnit); // source from compilation unit
            parser.setResolveBindings(true);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            cuConstructClassHierarchy = (CompilationUnit) parser.createAST(null);
            if (cuConstructClassHierarchy != null && cuConstructClassHierarchy.getPackage() != null
                && cuConstructClassHierarchy.getPackage().getName() != null) {
              countNbClasses++;
              // tempFileWriter.write(callerClassName +"\n");
              cuConstructClassHierarchy.accept(new ASTVisitor() {
                /*
                 * We need to check the case where this class is inherited from other classes
                 * ClassDeclaration and InterfaceDeclaration
                 */
                @Override
                public boolean visit(TypeDeclaration node) {

                  nbTotalClasses++;
                  boolean isInner = true;
                  String className = node.resolveBinding().getQualifiedName();
                  if (className.equals(callerClassName)) {
                    isInner = false;
                  }
                  int type = ClassHierarchy.IMPLEMENTATION_CLASS; // get type of this class
                  if (node.isInterface()) {
                    // System.out.println("INTERFACE");
                    type = ClassHierarchy.INTERFACE;
                    countInterface++;
                    countInterfaceMethods += node.getMethods().length;
                  }
                  // System.out.println(Modifier.ABSTRACT + ":" );
                  List<IExtendedModifier> iexModifierList = node.modifiers(); // check if this is an
                                                                              // abstract class
                  if (iexModifierList != null) {
                    for (IExtendedModifier iexModifier : iexModifierList) {
                      // System.err.println("iexModifier " + iexModifier);
                      if (iexModifier.toString().equals("abstract")) {
                        // System.err.println(callerClassName);
                        type = ClassHierarchy.ABSTRACT_CLASS;
                        countAbstractClass++;
                        countAbstractMethods += node.getMethods().length;
                      }
                    }
                  }
                  if (type == ClassHierarchy.IMPLEMENTATION_CLASS) {
                    countImplementationClass++;
                    countImplementationMethods += node.getMethods().length;
                  }

                  ClassHierarchy classHierarchy = getClassHierarchy(type, isInner, className);
                  // System.err.println(node.resolveBinding().getQualifiedName() + " : " +
                  // className);
                  classHierarchy.setType(type);
                  classHierarchy.setIsInner(isInner);

                  countMethods += node.getMethods().length;
                  // for (MethodDeclaration methodDec : node.getMethods()) {
                  // // System.err.println("METHOD_DEC " +
                  // methodDec.getName().getFullyQualifiedName() + ":" +
                  // getMethodSignatureFromParameters(methodDec.parameters()));
                  // classHierarchy.addMethod(methodDec.getName().getFullyQualifiedName() + ":" +
                  // getMethodSignatureFromParameters(methodDec.parameters()));
                  // }
                  classHierarchy.setMethodDecs(node.getMethods());


                  // for (TypeDeclaration typeDec : node.getTypes()) {
                  // System.out.println("typeDec " + typeDec);
                  // }
                  // if (!node.isInterface()) { //we only analyze implementation class, might need
                  // to check interface later
                  boolean noInheritance = true;

                  Type superClassType = node.getSuperclassType(); // get super class
                  if (superClassType != null && superClassType.resolveBinding() != null) {
                    String baseClassName = superClassType.resolveBinding().getQualifiedName();
                    // System.err.println(className + " HAS SUPER " + baseClassName);
                    classHierarchy.addBaseClass(baseClassName);
                    ClassHierarchy baseClassHierarchy =
                        getClassHierarchy(ClassHierarchy.UNDEFINED, false, baseClassName);
                    baseClassHierarchy.addSubClass(className);
                    classHierarchyModel.put(baseClassName, baseClassHierarchy);
                    noInheritance = false;
                    // superClasses.add(superClassType.resolveBinding().getQualifiedName());
                  }
                  List<Type> superInterfaces = node.superInterfaceTypes();
                  if (superInterfaces != null) {
                    for (Type superType : superInterfaces) {
                      if (superType != null && superType.resolveBinding() != null) {
                        String baseClassName = superType.resolveBinding().getQualifiedName();
                        // System.err.println(className + " HAS INTERFACE " + baseClassName);
                        classHierarchy.addBaseClass(baseClassName);
                        ClassHierarchy baseClassHierarchy =
                            getClassHierarchy(ClassHierarchy.UNDEFINED, false, baseClassName);
                        baseClassHierarchy.addSubClass(className);
                        classHierarchyModel.put(baseClassName, baseClassHierarchy);
                        // superClasses.add(type.resolveBinding().getQualifiedName());
                        noInheritance = false;
                      }
                    }
                  }
                  if (!noInheritance && type == ClassHierarchy.INTERFACE) {
                    countNoInheritance++;
                  }
                  // }
                  // System.err.println("NAME " + className);
                  classHierarchyModel.put(className, classHierarchy);
                  // } else {
                  // countInnerClass++;
                  // boolean noInheritance = true;
                  // if (node.isInterface()) {
                  // // System.out.println("INTERFACE " + node.resolveBinding().getQualifiedName());
                  // noInheritance = false;
                  // countInnerInterface++;
                  // }
                  //
                  // List<IExtendedModifier> iexModifierList = node.modifiers();
                  // if (iexModifierList != null) {
                  // for (IExtendedModifier iexModifier : iexModifierList) {
                  // // System.err.println("iexModifier " + iexModifier);
                  // if (iexModifier.toString().equals("abstract")) {
                  // // System.out
                  // // .println("ABSTRACT CLASS " + node.resolveBinding().getQualifiedName());
                  // countInnerAbstractClass++;
                  // noInheritance = false;
                  // }
                  // }
                  // }
                  //
                  // if (noInheritance) {
                  // countInnerClass++;
                  // }

                  // System.out.println("DIFFERENT_NAME " + className);
                  // }
                  return true;

                }

                // public boolean visit(EnumDeclaration node) {
                // String className = node.resolveBinding().getQualifiedName();
                // if (className.equals(callerClassName)) { //
                // System.out.println("EnumDec " + className);
                // }
                // return true;
                // }

              });
            }
          }
        }
      }
    }
    System.out.println("NbClass " + countNbClasses);
    System.out.println("countImplementationClass : " + countImplementationClass);
    System.out.println("countAbstractClass :" + countAbstractClass);
    System.out.println("countInnerAbstractClass : " + countInnerAbstractClass);
    System.out.println("countInterface : " + countInterface);
    System.out.println("countInnerInterface : " + countInnerInterface);
    System.out.println("nbTotalClasses : " + nbTotalClasses);
    System.out.println("countInnerClass : " + countInnerClass);
    System.out.println("countNoInheritance : " + countNoInheritance);

    int countExternalInterfaceAbstractClasses = 0;
    int count1 = 0;
    int count2 = 0;
    int count3 = 0;
    for (String clName : classHierarchyModel.keySet()) {
      // System.out.println("CLASS NAME :::: " + clName);
      ClassHierarchy cH = classHierarchyModel.get(clName);
      if (cH.getType() == ClassHierarchy.UNDEFINED) {
        countExternalInterfaceAbstractClasses++;
      }
      if (cH.getType() == ClassHierarchy.IMPLEMENTATION_CLASS && !cH.getIsInner()
          && cH.getSubClasses().size() > 0) {
        // System.out.println(clName);
        count1++;
      }
      if (cH.getType() == ClassHierarchy.ABSTRACT_CLASS) {
        count2++;
      }
      if (cH.getType() == ClassHierarchy.INTERFACE) {
        count3++;
      }
    }
    System.out.println("Number of nodes in classHierarchyModel " + classHierarchyModel.size());
    System.out.println("countExternalInterfaceAbstractClasses : "
        + countExternalInterfaceAbstractClasses);
    System.out.println("No Implemenetation which are inherited by another class " + count1);
    System.out.println(("No Abstract " + count2));
    System.out.println("No Interface " + count3);

    System.out.println("countMethods : " + countMethods);
    System.out.println("countImplementationMethods : " + countImplementationMethods);
    System.out.println("countAbstractMethods : " + countAbstractMethods);
    System.out.println("countInterfaceMethods : " + countInterfaceMethods);

    // construct method inheritance
    // for (String className : classHierarchyModel.keySet()) {
    // ClassHierarchy cH = classHierarchyModel.get(className);
    // // if (cH.isImplementationClass() || cH.isAbstractClass()) {
    // if (cH.hasBaseClass()) {
    // HashMap<Integer, String> baseClasses = cH.getBaseClasses();
    // for (Integer baseClassId : baseClasses.keySet()) {
    // ClassHierarchy baseCH = classHierarchyModel.get(baseClasses.get(baseClassId));
    // for (String baseMethodName : baseCH.getMethodSet()) {
    // if (cH.checkExistingMethod(baseMethodName)) {
    // cH.updateBaseMethod(baseMethodName, baseClassId);
    // baseCH.updateSubMethod(baseMethodName, baseCH.getSubClassId(className));
    // classHierarchyModel.put(baseClasses.get(baseClassId), baseCH);
    // }
    // }
    // }
    // classHierarchyModel.put(className, cH);
    // }
    // // }
    // }
    // if (classHierarchyModel.get("org.apache.cassandra.net.IVerbHandler").getMethodDecSet().length
    // == 0) {
    // System.err.println("SOMETHING IS WRONG");
    // }

    int countOverriedFromInterface = 0;
    for (String className : classHierarchyModel.keySet()) {
      // System.err.println("HA " + className);
      // if (className.equals("org.apache.cassandra.io.util.DataIntegrityMetadata.ChecksumWriter"))
      // {
      // if
      // (className.equals("org.apache.cassandra.streaming.ConnectionHandler.IncomingMessageHandler"))
      // {
      // System.err.println("FOUNDIT");
      ClassHierarchy cH = classHierarchyModel.get(className);
      // if (className.contains("DatacenterAwareRequestCoordinator")) {
      HashSet<String> baseClassNames = new HashSet<String>();
      constructMultiLevelClassHierarchy(baseClassNames, className, className, cH, cH, 0);
      // System.out.println("baseClassNames size " + baseClassNames.size());
      for (String bClassName : baseClassNames) {
        // System.out.println("ADD BASE CLASS " + bClassName);
        cH.addBaseClass(bClassName);
      }
      // }
      // if (cH.hasBaseClass()) {
      // for (String baseClassName : cH.getBaseClasses()) {
      // // System.out.println("BASE_CLASS " + baseClassName);
      // ClassHierarchy baseCH = classHierarchyModel.get(baseClassName);
      // if (baseCH.getMethodDecSet() != null) {
      // for (MethodDeclaration baseMethodDec : baseCH.getMethodDecSet()) {
      // String methodName = baseMethodDec.getName().getFullyQualifiedName();
      // // System.out.println("methodName " + methodName);
      // // System.out.println(getMethodSignatureFromParameters(baseMethodDec.parameters()));
      // MethodDeclaration methodDec = cH.isOverride(methodName, baseMethodDec.resolveBinding(),
      // baseMethodDec.parameters());
      // if (methodDec != null) {
      // String methodSig = methodName + ":" +
      // getMethodSignatureFromParameters(methodDec.parameters());
      // String baseMethodSig = baseClassName + ":" +
      // getMethodSignatureFromParameters(baseMethodDec.parameters());
      // cH.updateBaseMethod(methodSig, baseMethodSig );
      // baseCH.updateSubMethod(baseMethodSig, methodSig);
      // classHierarchyModel.put(baseClassName, baseCH);
      // // if (baseCH.isInterface()) {
      // // countOverriedFromInterface++;
      // // this.classHierarchytmpFileWriter.write(className + " == " + methodSig + " == " +
      // baseMethodSig + "\n");
      // // }
      // }
      // }
      // }
      // }
      // classHierarchyModel.put(className, cH);
      // }
      // }
      // }
    }
    System.out.println("countOverriedFromInterface : " + countOverriedFromInterface);
    int countInheritedMethods = 0;
    int countInheritedClassButNotMethods = 0;
    int counterr = 0;
    for (String className : classHierarchyModel.keySet()) {
      // if (className.equals("org.apache.cassandra.io.util.DataIntegrityMetadata.ChecksumWriter"))
      // {
      ClassHierarchy cH = classHierarchyModel.get(className);
      if (cH.hasBaseClass()) {
        // System.out.println("FFFFF " + className);
        // if (cH.getBaseMethodInformartionSize() == 0) {
        // countInheritedClassButNotMethods++;
        String baseClasses = cH.toString();
        if (!baseClasses.isEmpty()) {
          this.classHierarchytmpFileWriter.write("CLASS " + className + "\n");
          this.classHierarchytmpFileWriter.write(cH.toString());
          this.classHierarchytmpFileWriter.write("===\n");
        }
        // this.classHierarchytmpFileWriter.write(cH.printBaseMethodInformation());

        // } else {
        // this.classHierarchytmpFileWriter.write(className + "\n");
        // this.classHierarchytmpFileWriter.write(cH.toString());
        // countInheritedMethods += cH.getBaseMethodInformartionSize();
        // HashMap<String, Set<String>> baseMethodInformation = cH.getBaseMethodInformation();
        // for (String methodName : baseMethodInformation.keySet()) {
        // for (String baseMethodClassSignature : baseMethodInformation.get(methodName)) {
        // this.classHierarchytmpFileWriter.write(methodName + " from " + baseMethodClassSignature +
        // "\n");
        // }
        // }
        // this.classHierarchytmpFileWriter.write("===\n");
        // }
      }
      // }
    }
    System.out.println("countInheritedMethods: " + countInheritedMethods);
    System.out.println("countInheritedClassButNotMethods: " + countInheritedClassButNotMethods);
    this.classHierarchytmpFileWriter.close();
  }
}
