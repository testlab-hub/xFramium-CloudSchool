package org.xframium.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.xpath.XPathFactory;

import org.openqa.selenium.Platform;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.xframium.Initializable;
import org.xframium.application.ApplicationDescriptor;
import org.xframium.application.ApplicationRegistry;
import org.xframium.application.ApplicationVersion;
import org.xframium.application.CSVApplicationProvider;
import org.xframium.application.ExcelApplicationProvider;
import org.xframium.application.SQLApplicationProvider;
import org.xframium.application.XMLApplicationProvider;
import org.xframium.container.ApplicationContainer;
import org.xframium.container.CloudContainer;
import org.xframium.container.DeviceContainer;
import org.xframium.container.DriverContainer;
import org.xframium.container.FavoriteContainer;
import org.xframium.container.PageContainer;
import org.xframium.container.SiteContainer;
import org.xframium.container.SuiteContainer;
import org.xframium.container.TagContainer;
import org.xframium.content.ContentManager;
import org.xframium.content.provider.ExcelContentProvider;
import org.xframium.content.provider.SQLContentProvider;
import org.xframium.content.provider.XMLContentProvider;
import org.xframium.device.DeviceManager;
import org.xframium.device.SimpleDevice;
import org.xframium.device.cloud.CSVCloudProvider;
import org.xframium.device.cloud.CloudDescriptor;
import org.xframium.device.cloud.CloudProvider;
import org.xframium.device.cloud.CloudRegistry;
import org.xframium.device.cloud.EncryptedCloudProvider;
import org.xframium.device.cloud.ExcelCloudProvider;
import org.xframium.device.cloud.SQLCloudProvider;
import org.xframium.device.cloud.XMLCloudProvider;
import org.xframium.device.data.CSVDataProvider;
import org.xframium.device.data.DataProvider.DriverType;
import org.xframium.device.data.ExcelDataProvider;
import org.xframium.device.data.NamedDataProvider;
import org.xframium.device.data.SQLDataProvider;
import org.xframium.device.data.XMLDataProvider;
import org.xframium.device.data.perfectoMobile.AvailableHandsetValidator;
import org.xframium.device.data.perfectoMobile.PerfectoMobileDataProvider;
import org.xframium.device.data.perfectoMobile.PerfectoMobilePluginProvider;
import org.xframium.device.data.perfectoMobile.ReservedHandsetValidator;
import org.xframium.device.property.PropertyAdapter;
import org.xframium.device.proxy.ProxyRegistry;
import org.xframium.driver.xsd.ObjectFactory;
import org.xframium.driver.xsd.XActivity;
import org.xframium.driver.xsd.XArtifact;
import org.xframium.driver.xsd.XDevice;
import org.xframium.driver.xsd.XDeviceCapability;
import org.xframium.driver.xsd.XElement;
import org.xframium.driver.xsd.XElementParameter;
import org.xframium.driver.xsd.XFramiumRoot;
import org.xframium.driver.xsd.XFunction;
import org.xframium.driver.xsd.XInitiator;
import org.xframium.driver.xsd.XLibrary;
import org.xframium.driver.xsd.XModel;
import org.xframium.driver.xsd.XPackage;
import org.xframium.driver.xsd.XPackages;
import org.xframium.driver.xsd.XPage;
import org.xframium.driver.xsd.XParameter;
import org.xframium.driver.xsd.XProperty;
import org.xframium.driver.xsd.XPropertyAdapter;
import org.xframium.driver.xsd.XReference;
import org.xframium.driver.xsd.XSimpleElement;
import org.xframium.driver.xsd.XStep;
import org.xframium.driver.xsd.XTag;
import org.xframium.driver.xsd.XTest;
import org.xframium.driver.xsd.XToken;
import org.xframium.driver.xsd.XValidator;
import org.xframium.page.BY;
import org.xframium.page.ElementDescriptor;
import org.xframium.page.Page;
import org.xframium.page.PageManager;
import org.xframium.page.activity.ActivityInitiator;
import org.xframium.page.activity.ActivityValidator;
import org.xframium.page.activity.PageActivity;
import org.xframium.page.data.PageDataManager;
import org.xframium.page.data.provider.ExcelPageDataProvider;
import org.xframium.page.data.provider.PageDataProvider;
import org.xframium.page.data.provider.SQLPageDataProvider;
import org.xframium.page.data.provider.XMLPageDataProvider;
import org.xframium.page.element.Element;
import org.xframium.page.element.ElementFactory;
import org.xframium.page.element.SubElement;
import org.xframium.page.element.provider.CSVElementProvider;
import org.xframium.page.element.provider.ElementProvider;
import org.xframium.page.element.provider.ExcelElementProvider;
import org.xframium.page.element.provider.QAFElementProvider;
import org.xframium.page.element.provider.SQLElementProvider;
import org.xframium.page.element.provider.XMLElementProvider;
import org.xframium.page.keyWord.GherkinContainer;
import org.xframium.page.keyWord.KeyWordDriver;
import org.xframium.page.keyWord.KeyWordDriver.TRACE;
import org.xframium.page.keyWord.KeyWordPage;
import org.xframium.page.keyWord.KeyWordParameter;
import org.xframium.page.keyWord.KeyWordParameter.ParameterType;
import org.xframium.page.keyWord.KeyWordStep;
import org.xframium.page.keyWord.KeyWordStep.StepFailure;
import org.xframium.page.keyWord.KeyWordStep.ValidationType;
import org.xframium.page.keyWord.KeyWordTest;
import org.xframium.page.keyWord.KeyWordToken;
import org.xframium.page.keyWord.KeyWordToken.TokenType;
import org.xframium.page.keyWord.gherkinExtension.XMLFormatter;
import org.xframium.page.keyWord.matrixExtension.MatrixTest;
import org.xframium.page.keyWord.provider.ExcelKeyWordProvider;
import org.xframium.page.keyWord.provider.GherkinKeyWordProvider;
import org.xframium.page.keyWord.provider.SQLKeyWordProvider;
import org.xframium.page.keyWord.provider.XMLKeyWordProvider;
import org.xframium.page.keyWord.provider.gherkin.CommonGherkin;
import org.xframium.page.keyWord.step.KeyWordStepFactory;
import org.xframium.spi.Device;

import cucumber.api.java.en.And;
import cucumber.api.java.en.But;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gherkin.parser.Parser;

public class XMLConfigurationReader extends AbstractConfigurationReader implements ElementProvider
{
	private static final String[] PROXY_SETTINGS = new String[] { "proxy.host", "proxy.port", "proxy.ignoreHost" };
    private static final String[] JDBC = new String[] { "jdbc.username", "jdbc.password", "jdbc.url", "jdbc.driverClassName" };
    private static final String[] OPT_CLOUD = new String[] { "cloudRegistry.query" };
    private static final String[] OPT_APP = new String[] { "applicationRegistry.query", "applicationRegistry.capQuery" };
    private static final String[] OPT_PAGE = new String[] { "pageManagement.query" };
    private static final String[] OPT_DATA = new String[] { "pageManagement.pageData.query" };
    private static final String[] OPT_CONTENT = new String[] { "pageManagement.content.query" };
    private static final String[] OPT_DEVICE = new String[] { "deviceManagement.device.query", "deviceManagement.capa bility.query" };
    private static final String[] OPT_DRIVER = new String[] { "driver.suiteName", "driver.modelQuery", "driver.testSuiteQuery", "driver.testCaseQuery" };
    private XFramiumRoot xRoot;
    private Map<String,String> configProperties = new HashMap<String,String>( 10 );
    
    private Map<String,Element> elementMap = new HashMap<String,Element>(20);
    
    private static XPathFactory xPathFactory = XPathFactory.newInstance();
    private String siteName;
    
    private Map<String,SiteContainer> siteTree = new HashMap<String,SiteContainer>( 20 );
    private List<SiteContainer> siteList = new ArrayList<SiteContainer>( 10 );
    
    private boolean pageInitialized = false;
    
    @Override
    public boolean isInitialized()
    {
        return pageInitialized;
    }
    
    protected Map<String,String> getConfigurationProperties()
    {
        return configProperties;
    }
    
    @Override
    protected boolean readReferences( SuiteContainer sC ) 
    {
    		if ( xRoot.getReferences() != null && xRoot.getReferences().getReference() != null && xRoot.getReferences().getReference().size() > 0 )
    		{
    			for ( XReference xR : xRoot.getReferences().getReference() )
    			{
    				if ( !xR.isActive() )
    					continue;
    				XMLConfigurationReader xReader = new XMLConfigurationReader();
    				xReader.setxFID( xFID );
    				xReader.readFile( findFile(configFolder, new File( xR.getFileName() ) ) );
    				
    				PageDataProvider pD = null;
    				
    				
    				if ( xR.isData() )
    				{
    					pD = xReader.configureData();
    					if ( pD != null )
    					{
    						if ( PageDataManager.instance( xFID ).getDataProvider() == null )
    							PageDataManager.instance( xFID ).setPageDataProvider( pD );
    						else
    						{
    							PageDataManager.instance( xFID ).getDataProvider().addPageData( pD );
    							pD = PageDataManager.instance( xFID ).getDataProvider();
    						}
    					}
    				}
    				
    				if ( xR.isElements() )
    				{
    					ElementProvider eP = xReader.configurePageManagement( null );
    					if ( eP != null )
    						PageManager.instance( xFID ).getElementProvider().addElementProvider( eP );
    					
    				}
    				
    				if ( xR.isFunctions() || xR.isTests() )
    				{
    					SuiteContainer s2 = xReader.configureTestCases( pD, true );
    					if ( xR.isTests() )
    					{
    						sC.getActiveTestList().addAll( s2.getActiveTestList() );
    						sC.getInactiveTestList().addAll( s2.getInactiveTestList() );
    					}
    					
    					if ( xR.isFunctions() )
    						sC.getFunctionList().addAll( s2.getFunctionList() );
    					
    				}
    				
    				if ( xR.isApplications() )
    				{
    					for ( ApplicationDescriptor aD : xReader.configureApplication().getAppList() )
    					{
    						ApplicationRegistry.instance( xFID ).addApplicationDescriptor( aD );
    					}
    				}
    				
    				if ( xR.isDevices() )
    				{
    					DeviceContainer dC = xReader.configureDevice();
    					for ( Device d : dC.getActiveDevices() )
    		                DeviceManager.instance( xFID ).registerDevice( d );
    		            
    		            for ( Device d : dC.getInactiveDevices() )
    		                DeviceManager.instance( xFID ).registerInactiveDevice( d );
    				}
    				
    				
    				
    			}
    		}
    		return true;
    }
    
    @Override
    public boolean readFile( InputStream inputStream )
    {
        try
        {
            
            JAXBContext jc = JAXBContext.newInstance( ObjectFactory.class );
            Unmarshaller u = jc.createUnmarshaller();
            JAXBElement<?> rootElement = (JAXBElement<?>) u.unmarshal( inputStream );

            xRoot = (XFramiumRoot) rootElement.getValue();
            
            for ( XProperty currentProp : xRoot.getDriver().getProperty() )
            {
                configProperties.put( currentProp.getName(), currentProp.getValue() );
            }
            return true;
        }
        catch ( Exception e )
        {
            log.fatal( "Error reading CSV Element File", e );
            return false;
        }
    }
    
    @Override
    public boolean readFile( File configFile )
    {
        try
        {
            configFolder = configFile.getParentFile();
            return readFile( new FileInputStream( configFile ) );
        }
        catch ( Exception e )
        {
            log.fatal( "Error reading configuration File", e );
            return false;
        }
       
    }

    @Override
    public FavoriteContainer configureFavorites()
    {
        String favorites = xRoot.getFavorites();
        
        return new FavoriteContainer( favorites );
        
    }
    
    @Override
    public CloudContainer configureCloud( boolean secured )
    {
        CloudContainer cC = new CloudContainer();
        CloudProvider cloudProvider = null;
        switch ( xRoot.getCloud().getProvider() )
        {
            case "XML":
                
                cloudProvider = new XMLCloudProvider( findFile( configFolder, new File( xRoot.getCloud().getFileName() ) ) );
                break;

            case "SQL":
                cloudProvider = new SQLCloudProvider( configProperties.get( JDBC[0] ),
                                                                                 configProperties.get( JDBC[1] ),
                                                                                 configProperties.get( JDBC[2] ),
                                                                                 configProperties.get( JDBC[3] ),
                                                                                 configProperties.get( OPT_CLOUD[0] ));
                break;

            case "CSV":
                cloudProvider = new CSVCloudProvider( findFile( configFolder, new File( xRoot.getCloud().getFileName() ) ) );
                break;

            case "EXCEL":
                cloudProvider = new ExcelCloudProvider( findFile( configFolder, new File( xRoot.getCloud().getFileName() ) ), configProperties.get( "cloudRegistry.tabName" ) );
                break;
                
            case "LOCAL":
                CloudDescriptor cloud = new CloudDescriptor( xRoot.getCloud().getName(), EncryptedCloudProvider.decryptValue( xRoot.getCloud().getUserName() ), EncryptedCloudProvider.decryptValue( xRoot.getCloud().getPassword() ), xRoot.getCloud().getHostName(), xRoot.getCloud().getProxyHost(), xRoot.getCloud().getProxyPort().intValue() + "", "", xRoot.getCloud().getGrid(), xRoot.getCloud().getProviderType(), xRoot.getCloud().getGesture(), xRoot.getCloud().getDeviceAction() );
                cC.setCloudList( new ArrayList<CloudDescriptor>( 10 ) );
                cC.getCloudList().add( cloud );
                break;
        }

        if ( !xRoot.getCloud().getProvider().equals( "LOCAL" ) )
        {
            if ( secured )
                cloudProvider = new EncryptedCloudProvider( cloudProvider );
            
            cC.setCloudList( cloudProvider.readData() );
        }
        cC.setCloudName( xRoot.getCloud().getName() );
        
        return cC;
    }

    @Override
    public ApplicationContainer configureApplication()
    {
        ApplicationContainer appContainer = new ApplicationContainer();
        
        switch ( xRoot.getApplication().getProvider() )
        {
            case "XML":
                appContainer.setAppList( new XMLApplicationProvider( findFile( configFolder, new File( xRoot.getApplication().getFileName() ) ) ).readData() );
                break;

            case "CSV":
                appContainer.setAppList( new CSVApplicationProvider( findFile( configFolder, new File( xRoot.getApplication().getFileName() ) ) ).readData() );
                break;
                
            case "SQL":
                appContainer.setAppList( new SQLApplicationProvider( configProperties.get( JDBC[0] ),
                                                                                                   configProperties.get( JDBC[1] ),
                                                                                                   configProperties.get( JDBC[2] ),
                                                                                                   configProperties.get( JDBC[3] ),
                                                                                                   configProperties.get( OPT_APP[0] ),
                                                                                                   configProperties.get( OPT_APP[1] )).readData());
                break;

            case "EXCEL":
                appContainer.setAppList( new ExcelApplicationProvider( findFile( configFolder, new File( xRoot.getApplication().getFileName() ) ), configProperties.get( "applicationRegistry.tabName" ) ).readData() );
                break;
                
            case "LOCAL":
                appContainer.getAppList().add( new ApplicationDescriptor( xRoot.getApplication().getName(), "", xRoot.getApplication().getAppPackage(), xRoot.getApplication().getBundleId(), xRoot.getApplication().getUrl(), xRoot.getApplication().getIosInstall(), xRoot.getApplication().getAndroidInstall(), createCapabilities( xRoot.getApplication().getCapability() ), xRoot.getApplication().getVersion(), xRoot.getApplication().getEnvironment(), xRoot.getApplication().isAutoStart() ) );
                break;
        }
        appContainer.setApplicationName( xRoot.getApplication().getName() );
        return appContainer;
    }

    private Map<String,Object> createCapabilities( List<XDeviceCapability> capabilityList )
    {
        Map<String,Object> capabilities = new HashMap<String,Object>( 5 );
        if ( capabilityList != null )
        {
            for ( XDeviceCapability cap : capabilityList )
            {
                switch ( cap.getClazz() )
                {
                    case "BOOLEAN":
                        capabilities.put( cap.getName(), Boolean.parseBoolean( cap.getValue() ) );
                        break;
    
                    case "OBJECT":
                        capabilities.put( cap.getName(), cap.getValue() );
                        break;
    
                    case "STRING":
                        capabilities.put( cap.getName(), cap.getValue() );
                        break;
    
                    case "PLATFORM":
                        capabilities.put( cap.getName(), Platform.valueOf( cap.getValue().toUpperCase() ) );
                        break;
                        
                    case "CLASS":
                    	try
                    	{
                    		capabilities.put( cap.getName(), Class.forName( cap.getValue() ).newInstance() );
	                    	break;
                    	}
                    	catch( Exception e )
                    	{
                    		log.error( "Could not create Object Instance as " + cap.getValue(), e );
                    	}
                }
            }
        }
        
        return capabilities;
    }
    
    @Override
    protected boolean configureThirdParty()
    {
        for ( XLibrary lib : xRoot.getDriver().getLibrary() )
        {;
            try
            {
                log.info( "Configuring Third Party support for " + lib.getName() + " as " + lib.getClassName() );

                Initializable newExtension = (Initializable) Class.forName( lib.getClassName() ).newInstance();
                Properties props = new Properties();
                props.putAll( configProperties );
                newExtension.initialize( lib.getName(), configProperties );

            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        
        return true;
    }

    @Override
    public boolean configureArtifacts( DriverContainer driverC )
    {
        driverC.setReportFolder( xRoot.getDriver().getOutputFolder() );
        
        for ( XArtifact a : xRoot.getDriver().getArtifact() )
        {
            if ( a.isActive() )
                driverC.addArtifact( a.getType().toUpperCase() );
        }
        
        return true;
        
        
    }
    
    @Override
    public String getSiteName()
    {
        return siteName;
    }

    @Override
    public ElementProvider configurePageManagement( SuiteContainer sC )
    {
        if ( sC != null )
            sC.setSiteName( xRoot.getModel().getSiteName() );
        siteName = xRoot.getModel().getSiteName();

        switch ( xRoot.getModel().getProvider() )
        {
            case "XML":

                return new XMLElementProvider( findFile( configFolder, new File( xRoot.getModel().getFileName() ) ) );
                
            case "QUANTUM":

                return new QAFElementProvider( findFile( configFolder, new File( xRoot.getModel().getFileName() ) ) );

            case "SQL":
                return new SQLElementProvider( configProperties.get( JDBC[0] ),
                                                                                   configProperties.get( JDBC[1] ),
                                                                                   configProperties.get( JDBC[2] ),
                                                                                   configProperties.get( JDBC[3] ),
                                                                                   configProperties.get( OPT_PAGE[0] ));

            case "CSV":
                return new CSVElementProvider( findFile( configFolder, new File( xRoot.getModel().getFileName() ) ) );

            case "EXCEL":
                String[] fileNames = xRoot.getModel().getFileName().split( "," );

                File[] files = new File[fileNames.length];
                for ( int i = 0; i < fileNames.length; i++ )
                    files[i] = findFile( configFolder, new File( fileNames[i] ) );

                return new ExcelElementProvider( files, xRoot.getModel().getSiteName() );
                
            case "LOCAL":
                ElementDescriptor elementDescriptor = null;
                Element currentElement = null;
                boolean elementsRead = true;
                for ( XPage page : xRoot.getModel().getPage() )
                {
                    for ( XElement ele : page.getElement() )
                    {
                        elementDescriptor = new ElementDescriptor( page.getSite() != null && page.getSite().trim().length() > 0 ? page.getSite() : xRoot.getModel().getSiteName(), page.getName(), ele.getName() );
                        currentElement = ElementFactory.instance().createElement( BY.valueOf( ele.getDescriptor() ), ele.getValue(), ele.getName(), page.getName(), ele.getContextName() );
                        
                        if ( ele.getElement() != null )
                        {
                            //
                            // Any element can contain sub elements to allow for a finer lookup
                            //
                            for ( XSimpleElement sE : ele.getElement() )
                            {
                                SubElement subElement = new SubElement( BY.valueOf( sE.getDescriptor() ), sE.getValue(), sE.getOs(), sE.getVersion( ) == null ? null : new ApplicationVersion( sE.getVersion() ), sE.getCloud(), sE.getDeviceTag(), sE.getContext() );
                                currentElement.addSubElement( subElement );
                                if ( sE.getParameter() != null )
                                {
                                    for ( XElementParameter xP : ele.getParameter() )
                                    {
                                        subElement.addProperty( xP.getName(), xP.getValue() );
                                    }
                                }
                            }
                        }
                        
                        
                        
                        if ( ele.getDeviceContext() != null && !ele.getDeviceContext().trim().isEmpty() )
                            currentElement.setDeviceContext( ele.getDeviceContext() );
                        
                        if ( ele.getParameter() != null )
                        {
                            for ( XElementParameter xP : ele.getParameter() )
                            {
                                currentElement.addElementProperty( xP.getName(), xP.getValue() );
                            }
                        }
                        
                        if (log.isDebugEnabled())
                            log.debug( "Adding XML Element using [" + elementDescriptor.toString() + "] as [" + currentElement + "]" );
                        boolean elementRead = true;
                        try
                        {
                            if ( currentElement.getBy() == BY.XPATH )
                                xPathFactory.newXPath().compile( currentElement.getKey().replace( "{", "" ).replace( "}", "" ) );
                            
                            SiteContainer siteContainer = siteTree.get( elementDescriptor.getSiteName() );
                            
                            if ( siteContainer == null )
                            {
                                siteContainer = new SiteContainer( elementDescriptor.getSiteName() );
                                siteTree.put( siteContainer.getSiteName(), siteContainer );
                                siteList.add( siteContainer );
                            }
                            
                            PageContainer elementList = siteContainer.getPage( elementDescriptor.getPageName(), page.getClazz() );
                            elementList.getElementList().add( currentElement );
                            
                            elementMap.put(elementDescriptor.toString(), currentElement );
                        }
                        catch( Exception e )
                        {
                            log.fatal( "Could not process page element identified by [" + elementDescriptor.toString() + "] as [" + currentElement.getKey() + "]" );
                            elementRead = false;
                        }
                        
                        elementsRead = elementsRead & elementRead;
                    }
                    if ( page.getActivity() != null )
                    {
                        for( XActivity activity : page.getActivity() )
                        {
                            PageActivity pageActivity = new PageActivity( activity.getName(), activity.getPage() );
                            
                            if ( activity.getInitiator() != null )
                            {
                                for ( XInitiator initiator : activity.getInitiator() )
                                    pageActivity.getInitiatorList().add( new ActivityInitiator( initiator.getElement(), initiator.getAction() ) );
                            }
                            
                            if ( activity.getValidator() != null )
                            {
                                for ( XValidator validator : activity.getValidator() )
                                    pageActivity.getValidatorList().add( new ActivityValidator( validator.getElement(), validator.getAction() ) );
                            }
                        }
                    }
                }
                
                pageInitialized = elementsRead;

               return this;
        }

        return null;
    }
    
    @Override
    public PageDataProvider configureData()
    {
        if ( xRoot.getData() == null )
            return null;
        
        PageDataProvider dataProvider = null;
        
        switch ( xRoot.getData().getProvider() )
        {
            case "XML":
                dataProvider = new XMLPageDataProvider( findFile( configFolder, new File( xRoot.getData().getFileName() ) ) );
                break;

            case "SQL":
                dataProvider =  new SQLPageDataProvider( configProperties.get( JDBC[0] ),
                                                             configProperties.get( JDBC[1] ),
                                                             configProperties.get( JDBC[2] ),
                                                             configProperties.get( JDBC[3] ),
                                                             configProperties.get( OPT_DATA[0] ));
                break;
                
            case "EXCEL":
                String[] fileNames = xRoot.getData().getFileName().split( "," );

                File[] files = new File[fileNames.length];
                for ( int i = 0; i < fileNames.length; i++ )
                    files[i] = findFile( configFolder, new File( fileNames[i] ) );
                
                dataProvider = new ExcelPageDataProvider( files, configProperties.get( "pageManagement.pageData.tabNames" ) );
                break;

        }
        
        return dataProvider;
    }

    @Override
    public boolean configureContent()
    {
        if ( xRoot.getContent() == null )
            return true;
        switch ( xRoot.getContent().getProvider() )
        {
            case "XML":
                ContentManager.instance(xFID).setContentProvider( new XMLContentProvider( findFile( configFolder, new File( xRoot.getContent().getFileName() ) ) ) );
                break;

            case "SQL":
                ContentManager.instance(xFID).setContentProvider( new SQLContentProvider( configProperties.get( JDBC[0] ),
                                                                                      configProperties.get( JDBC[1] ),
                                                                                      configProperties.get( JDBC[2] ),
                                                                                      configProperties.get( JDBC[3] ),
                                                                                      configProperties.get( OPT_CONTENT[0] )));
                break;

            case "EXCEL":

                int keyColumn = Integer.parseInt( configProperties.get( "pageManagement.content.keyColumn" ) );
                String[] lookupString = configProperties.get( "pageManagement.content.lookupColumns" ).split( "," );

                int[] lookupColumns = new int[lookupString.length];
                for ( int i = 0; i < lookupString.length; i++ )
                    lookupColumns[i] = Integer.parseInt( lookupString[i].trim() );

                ContentManager.instance(xFID).setContentProvider( new ExcelContentProvider( findFile( configFolder, new File( xRoot.getContent().getFileName() ) ), configProperties.get( "pageManagement.content.tabName" ), keyColumn, lookupColumns ) );
                break;

        }
        return true;
    }

    @Override
    public DeviceContainer configureDevice()
    {
        DeviceContainer dC = new DeviceContainer();
        List<Device> deviceList = null;
        switch ( xRoot.getDevices().getProvider() )
        {
            case "PERFECTO_PLUGIN":
                deviceList = new PerfectoMobilePluginProvider( configProperties.get( "deviceManagement.deviceList" ) + "", DriverType.valueOf( xRoot.getDriver().getType() ), configProperties.get( "deviceManagement.pluginType" ) ).readData( xFID );
                break;
            
            case "RESERVED":
                deviceList = new PerfectoMobileDataProvider( new ReservedHandsetValidator( CloudRegistry.instance( xFID ).getCloud().getUserName() ), DriverType.valueOf( xRoot.getDriver().getType() ) ).readData( xFID );
                break;

            case "AVAILABLE":
                deviceList = new PerfectoMobileDataProvider( new AvailableHandsetValidator(), DriverType.valueOf( xRoot.getDriver().getType() ) ).readData( xFID );
                break;

            case "CSV":
                deviceList = new CSVDataProvider( findFile( configFolder, new File( xRoot.getDevices().getFileName() ) ), DriverType.valueOf( xRoot.getDriver().getType() ) ).readData( xFID );
                break;

            case "XML":

                deviceList =  new XMLDataProvider( findFile( configFolder, new File( xRoot.getDevices().getFileName() ) ), DriverType.valueOf( xRoot.getDriver().getType() ) ).readData( xFID );
                break;

            case "SQL":
                deviceList = new SQLDataProvider( configProperties.get( JDBC[0] ),
                                                                      configProperties.get( JDBC[1] ),
                                                                      configProperties.get( JDBC[2] ),
                                                                      configProperties.get( JDBC[3] ),
                                                                      configProperties.get( OPT_DEVICE[0] ),
                                                                      configProperties.get( OPT_DEVICE[1] ),
                                                                      DriverType.valueOf( xRoot.getDriver().getType())).readData( xFID );
                break;

            case "EXCEL":
                deviceList = new ExcelDataProvider( findFile( configFolder, new File( xRoot.getDevices().getFileName() ) ), configProperties.get( "deviceManagement.tabName" ), DriverType.valueOf( xRoot.getDriver().getType() ) ).readData( xFID );
                break;

            case "NAMED":
                String devices = configProperties.get( "deviceManagement.deviceList" );
                if ( devices == null )
                {
                    System.err.println( "******* Property [deviceManagement.deviceList] was not specified" );
                    System.exit( -1 );
                }
                deviceList = new NamedDataProvider( devices, DriverType.valueOf( xRoot.getDriver().getType() ) ).readData( xFID );
                break;
                
            case "LOCAL":
                deviceList = new ArrayList<Device>( 10 );
                for ( XDevice device : xRoot.getDevices().getDevice() )
                {
                    deviceList.add( parseDevice( device ) );
                }
                
        }

        dC.setdType( DriverType.valueOf( xRoot.getDriver().getType() ) );
        
        if ( deviceList != null )
        {
            String deviceOverride = configProperties.get( "deviceOverrides" );
            if ( deviceOverride != null )
            {
                Map<String,Boolean> deviceMap = new HashMap<String,Boolean>( 10 );
                String[] overrideArray = deviceOverride.split( "," );
                for ( String override : overrideArray )
                    deviceMap.put( override, true );
                
                for ( Device d : deviceList )
                {
                    if ( deviceMap.containsKey( d.getKey() ) )
                    {
                        d.setActive( true );
                        dC.addDevice( d );
                    }
                    else
                    {
                        d.setActive( false );
                        dC.addDevice( d );
                    }
                }
            }
            else
            {
                for ( Device d : deviceList )
                    dC.addDevice( d );
            }
        }
        
        return dC;

    }
    
    private Device parseDevice( XDevice device )
    {
        String driverName = "";
        switch( xRoot.getDriver().getType() )
        {
            case "APPIUM":
                if ( device.getOs().toUpperCase().equals( "IOS" ) )
                    driverName = "IOS";
                else if ( device.getOs().toUpperCase().equals( "ANDROID" ) )
                    driverName = "ANDROID";
                else
                    log.warn( "Appium is not supported on the following OS " + device.getOs().toUpperCase() + " - this device will be ignored" );
                break;
                
            default:
                driverName = xRoot.getDriver().getType();
                break;
        }
        
        SimpleDevice currentDevice = new SimpleDevice(device.getName(), device.getManufacturer(), device.getModel(), device.getOs(), device.getOsVersion(), device.getBrowserName(), device.getBrowserVersion(), device.getAvailableDevices().intValue(), driverName, device.isActive(), device.getId() );
        if ( device.getCloud() != null && !device.getCloud().isEmpty() )
            currentDevice.setCloud( device.getCloud() );
        
        if ( device.getTagNames() != null && !device.getTagNames().trim().isEmpty() )
            currentDevice.setTagNames( device.getTagNames().split( "," ) );
        
        List<Object> list = null;
        String factoryName = null;
        Map<String, Object> keyOptions = null;
        Map<String, Object> browserOptionMap = null;
        
        if ( device.getCapability() != null )
        {
            for ( XDeviceCapability cap : device.getCapability() )
            {
                switch( cap.getClazz() )
                {
                    case "BOOLEAN":
                        currentDevice.addCapability( cap.getName(), Boolean.parseBoolean( cap.getValue() ), cap.getClazz() );
                        break;
                        
                    case "STRING":
                        currentDevice.addCapability( cap.getName(), cap.getValue(), cap.getClazz() );
                        break;
                        
                    case "PLATFORM":
                        currentDevice.addCapability( cap.getName(), Platform.valueOf( cap.getValue().toUpperCase() ), cap.getClazz() );
                        break;
                        
                    case "CLASS":
                    	try
                    	{
                    		currentDevice.addCapability( cap.getName(), Class.forName( cap.getValue() ).newInstance(), cap.getClazz() );
	                    	break;
                    	}
                    	catch( Exception e )
                    	{
                    		log.error( "Could not create Object Instance as " + cap.getValue(), e );
                    	}
                }
            }
            
        }

        return currentDevice;
        
    }

    @Override
    protected boolean configurePropertyAdapters()
    {
        for ( XPropertyAdapter xProp : xRoot.getDriver().getPropertyAdapter() )
        {
            try
            {
                DeviceManager.instance( xFID ).registerPropertyAdapter( (PropertyAdapter) Class.forName( xProp.getClassName() ).newInstance() );
            }
            catch( Exception e )
            {
                log.error( "Error creating proeprty adapter", e );
            }
        }
        
        DeviceManager.instance(xFID ).setConfigurationProperties( configProperties );
        DeviceManager.instance(xFID ).notifyPropertyAdapter( configProperties );
        return true;
    }

    @Override
    public SuiteContainer configureTestCases( PageDataProvider pdp, boolean parseDataIterators )
    {
        SuiteContainer sC = null;
        
        if ( xRoot.getSuite() == null )
        {
            sC = new SuiteContainer();
        }
        else
        {
                	
            switch ( xRoot.getSuite().getProvider() )
            {
                case "LOCAL":
                    sC = new SuiteContainer(); 
                    parseModel( sC, xRoot.getModel() );
                    for( XTest test : xRoot.getSuite().getTest() )
                    {
                        if ( sC.testExists( test.getName() ) )
                        {
                            log.warn( "The test [" + test.getName() + "] is already defined and will not be added again" );
                            continue;
                        }
                        
                        if ( test.getType().equals( "BDD" ) )
                        {
                            XMLFormatter xmlFormatter = new XMLFormatter( sC.getDataProvider(), configProperties, xFID );
                            Parser bddParser = new Parser( xmlFormatter );
                            bddParser.parse( test.getDescription().getValue(), "", 0 );
                            sC.setDataProvider( xmlFormatter );
                            PageDataManager.instance( xFID ).setPageDataProvider( xmlFormatter );
                        }
                        else if ( test.getType().equals( "CSV" ) )
                        {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append( test.getName() );
                            stringBuilder.append( ",Test,," );
                            stringBuilder.append( test.getDataProvider()!= null ? test.getDataProvider() : "" );
                            stringBuilder.append( "," );
                            stringBuilder.append( test.getDataDriver()!= null ? test.getDataDriver() : "" );
                            stringBuilder.append( "," );
                            stringBuilder.append( test.getTagNames() != null ? test.getTagNames() : "" );
                            stringBuilder.append( ",," );
                            stringBuilder.append( test.isTimed() );
                            stringBuilder.append( ",0," );
                            stringBuilder.append( test.isActive() );
                            stringBuilder.append( "," );
                            stringBuilder.append( test.getOs()!= null ? test.getOs() : "" );
                            
                            MatrixTest matrixTest = new MatrixTest( stringBuilder.toString(), test.getDescription().getValue() );
                            if ( matrixTest.isActive() )
                                sC.addActiveTest( matrixTest.createTest( configProperties ) );
                            else
                                sC.addInactiveTest( matrixTest.createTest( configProperties ) );
                        }
                        else if ( test.getType().equals( "XML" ) )
                        {
                            KeyWordTest currentTest = parseTest( test );
                            if ( currentTest.isActive() )
                                sC.addActiveTest( currentTest );
                            else
                                sC.addInactiveTest( currentTest );
                        }
                    }
                    
                    for( XFunction test : xRoot.getSuite().getFunction() )
                    {
                        if ( sC.testExists( test.getName() ) )
                        {
                            log.warn( "The function [" + test.getName() + "] is already defined and will not be added again" );
                            continue;
                        }
                        
                        sC.addFunction( parseFunction( test ) );
                    }
                    break;
                    
                    
                
                case "XML":
                    sC = new XMLKeyWordProvider( findFile( configFolder, new File( xRoot.getSuite().getFileName() ) ), configProperties ).readData( true );
    
                    break;
                
                case "GHERKIN":
                		String[] folders = xRoot.getSuite().getFileName().split( "," );
                		String[] packages = getValue( "gherkin.packages", "org.xframium", configProperties ).split( "," );
                	
                		File[] fileArray = new File[ folders.length ];
                		for ( int i=0; i<folders.length; i++ )
                			fileArray[ i ] = findFile( configFolder, new File( folders[ i ] ) );
                		
                    sC = new GherkinKeyWordProvider( fileArray, packages, xFID ).readData( true );
                    
                    
                    
                    
                    
                    if ( xRoot.getSuite().getPackages() == null )
                    	xRoot.getSuite().setPackages( new XPackages() );
                    
                    XPackage xP = new XPackage();
                    xP.setName( CommonGherkin.class.getPackage().getName() );
                    xP.setValue( "Common Gherkin functions provided by xFramium" );
                    xRoot.getSuite().getPackages().getPackage().add( xP );
    
                    break;
                    
                case "EXCEL":
                    sC =  new ExcelKeyWordProvider( findFile( configFolder, new File( xRoot.getSuite().getFileName() ) ), configProperties ).readData( true );
    
                    break;
    
                case "SQL":
                case "LOCAL-SQL":
                {
                    sC = new SQLKeyWordProvider( configProperties.get( JDBC[0] ),
                                                                                configProperties.get( JDBC[1] ),
                                                                                configProperties.get( JDBC[2] ),
                                                                                configProperties.get( JDBC[3] ),
                                                                                configProperties.get( OPT_DRIVER[0] ),
                                                                                xRoot.getModel().getSiteName(),
                                                                                configProperties.get( OPT_DRIVER[1] ),
                                                                                configProperties.get( OPT_DRIVER[2] ),
                                                                                configProperties.get( OPT_DRIVER[3] ),
                                                                                configProperties).readData( true ) ;
                                                       
                    break;
                }
            }
            
            if ( xRoot.getSuite().getPackages() != null && xRoot.getSuite().getPackages().getPackage() != null && !xRoot.getSuite().getPackages().getPackage().isEmpty() )
        	{
        		log.info( "Scanning for annotated methods" );
        		Map <String,GherkinContainer> methodMap = new HashMap<String,GherkinContainer>( 10 );

        		try
        		{
        			List<String> packageList = new ArrayList<String>( 20 );
        			for ( XPackage p : xRoot.getSuite().getPackages().getPackage() )
        			{
        				packageList.add( p.getName() );
        			}
        			
        			ConfigurationBuilder cB = new ConfigurationBuilder().forPackages(packageList.toArray( new String[ 0 ] ) ).setScanners( new MethodAnnotationsScanner() );
        			Reflections r = new Reflections( cB );
        			for ( Method m : r.getMethodsAnnotatedWith( Then.class ) )
        			{
        				log.info( "Adding GHERKIN [Then] as [" + m.getAnnotation( Then.class ).value() + "] for [" + m.getName() + "]" );
        				methodMap.put(m.getAnnotation( Then.class ).value() , new GherkinContainer( m.getAnnotation( Then.class ).value(), Pattern.compile( m.getAnnotation( Then.class ).value() ), m ) );
        			}
        			for ( Method m : r.getMethodsAnnotatedWith( But.class ) )
        			{
        				log.info( "Adding GHERKIN [But] as [" + m.getAnnotation( But.class ).value() + "] for [" + m.getName() + "]" );
        				methodMap.put(m.getAnnotation( But.class ).value() , new GherkinContainer( m.getAnnotation( But.class ).value(), Pattern.compile( m.getAnnotation( But.class ).value() ), m ) );
        			}
        			for ( Method m : r.getMethodsAnnotatedWith( And.class ) )
        			{
        				log.info( "Adding GHERKIN [And] as [" + m.getAnnotation( And.class ).value() + "] for [" + m.getName() + "]" );
        				methodMap.put(m.getAnnotation( And.class ).value() , new GherkinContainer( m.getAnnotation( And.class ).value(), Pattern.compile( m.getAnnotation( And.class ).value() ), m ) );
        			}
        			for ( Method m : r.getMethodsAnnotatedWith( Given.class ) )
        			{
        				log.info( "Adding GHERKIN [Given] as [" + m.getAnnotation( Given.class ).value() + "] for [" + m.getName() + "]" );
        				methodMap.put(m.getAnnotation( Given.class ).value() , new GherkinContainer( m.getAnnotation( Given.class ).value(), Pattern.compile( m.getAnnotation( Given.class ).value() ), m ) );
        			}
        			for ( Method m : r.getMethodsAnnotatedWith( When.class ) )
        			{
        				log.info( "Adding GHERKIN [When] as [" + m.getAnnotation( When.class ).value() + "] for [" + m.getName() + "]" );
        				methodMap.put(m.getAnnotation( When.class ).value() , new GherkinContainer( m.getAnnotation( When.class ).value(), Pattern.compile( m.getAnnotation( When.class ).value() ), m ) );
        			}

        			KeyWordDriver.instance(xFID).getMethodMap().putAll( methodMap );
        		}
        		catch( Exception e )
        		{
        			e.printStackTrace();
        		}
        	}
            
        }
        
        if ( sC != null && pdp != null )
        {
        	if ( sC.getDataProvider() != null )
        		pdp.addPageData( sC.getDataProvider() );
        	
        	sC.setDataProvider( pdp );
        }
        return sC;
    }
    
    @Override
    public DriverContainer configureDriver(Map<String, String> customConfig)
    {
        DriverContainer dC = new DriverContainer();
        
        if ( customConfig != null )
            configProperties.putAll( customConfig );
        
        String personaNames = getValue( "driver.personas", xRoot.getDriver().getPersonas(), configProperties ) ;
        if ( personaNames != null && !personaNames.isEmpty() )
            dC.setPerfectoPersonas( personaNames );
        
        dC.setDisplayReport(getValue( "driver.displayResults", xRoot.getDriver().getDisplayResults() + "", configProperties ) );
        dC.setSmartCaching( xRoot.getDriver().isCachingEnabled() );
        if ( xRoot.getSecurity() != null )
        {
            dC.setSecureCloud( xRoot.getSecurity().isSecureCloud() );
        }

        dC.setStepTags( getValue( "driver.stepTags", xRoot.getDriver().getStepTags(), configProperties ) );

        dC.getPropertyMap().putAll( configProperties );
        dC.setEmbeddedServer( Boolean.parseBoolean( getValue( "driver.embeddedServer", xRoot.getDriver().isEmbeddedServer() + "", configProperties ) ) );
        dC.setDriverType( DriverType.valueOf( xRoot.getDriver().getType() ) );
        
        if ( xRoot.getDependencies() != null )
        {
            dC.setBeforeDevice( getValue( "driver.beforeDevice", xRoot.getDependencies().getBeforeDevice(), configProperties ) );
            dC.setBeforeTest( getValue( "driver.beforeTest", xRoot.getDependencies().getBeforeTest(), configProperties ) );
            dC.setAfterTest( getValue( "driver.afterTest", xRoot.getDependencies().getAfterTest(), configProperties ) );
            dC.setBeforeStep( getValue( "driver.beforeStep", xRoot.getDependencies().getBeforeStep(), configProperties ) );
            dC.setAfterStep( getValue( "driver.afterStep", xRoot.getDependencies().getAfterStep(), configProperties ) );
        }
        
        if ( xRoot.getDriver().getExtractors() != null )
        {
            for ( XTag xTag : xRoot.getDriver().getExtractors() )
                dC.addExtractor( new TagContainer( xTag.getName(), xTag.getDescription().getValue(), xTag.getDescriptor() ) );
        }

        if ( dC.getExtractors().isEmpty() )
        {
            //
            // Add some default extractor types for new projects
            //
            dC.addExtractor( new TagContainer( "ANCHOR", "HTML anchor tags", "//a[@href!='#']" ) );
            dC.addExtractor( new TagContainer( "BUTTON", "HTML button tags", "//button" ) );
            dC.addExtractor( new TagContainer( "INPUT", "HTML Input Tags", "//input[@type!='hidden']" ) );
            dC.addExtractor( new TagContainer( "IOS button", "Apple IOS buttons", "//UIAButton" ) );
            dC.addExtractor( new TagContainer( "IOS switch", "Apple IOS buttons", "//UIASwitch" ) );
            dC.addExtractor( new TagContainer( "IOS table cell", "Apple IOS table cell", "//UIATableCell" ) );
            dC.addExtractor( new TagContainer( "IOS text field", "Apple IOS test field", "//UIATextField" ) );
            dC.addExtractor( new TagContainer( "Android button", "Android button", "//android.widget.Button" ) );
            dC.addExtractor( new TagContainer( "Android text field", "Android text field", "//android.widget.EditText" ) );
            dC.addExtractor( new TagContainer( "Windows Button", "A Windows XAML Button", "//Button" ) );
            dC.addExtractor( new TagContainer( "Windows Text", "A Windows XAML Text Field", "//Text" ) );
            dC.addExtractor( new TagContainer( "Windows List", "A Windows XAML List", "//List" ) );
        }

        //
        // Extract any named tests
        //
        dC.setTestNames( getValue( "driver.testNames", xRoot.getDriver().getTestNames(), configProperties ) );
        dC.setTestTags( getValue( "driver.tagNames", xRoot.getDriver().getTagNames(), configProperties ) );
        dC.setDryRun( xRoot.getDriver().isDryRun() );
        dC.setSuiteName( getValue( "driver.suiteName", xRoot.getDriver().getSuiteName(), configProperties ) );
        dC.setDomain( getValue( "driver.domain", xRoot.getDriver().getDomain(), configProperties ) );
        dC.setPhase( getValue( "driver.phase", xRoot.getDriver().getPhase(), configProperties ) );
        dC.setRetryCount( Integer.parseInt( getValue( "driver.retryCount", xRoot.getDriver().getRetryCount() + "", configProperties ) ) );
        dC.setTrace( getValue( "driver.trace", xRoot.getDriver().getTrace(), configProperties ) );
        
        String tagList = getValue( "driver.tagList", xRoot.getDriver().getExecutionName(), configProperties );
        if ( tagList != null )
            dC.setExecutionNames( tagList.split( "," ) );
        
        return dC;
    }
    
    private void parseModel( SuiteContainer sC, XModel model )
    {
        for ( XPage page : model.getPage() )
        {
            try
            {
                Class useClass = KeyWordPage.class;
                if ( page.getClazz() != null && !page.getClazz().isEmpty() )
                    useClass = ( Class<Page> ) Class.forName( page.getClazz() );
                
                if (log.isDebugEnabled())
                    log.debug( "Creating page as " + useClass.getSimpleName() + " for " + page.getName() );
    
                sC.addPageModel( page.getSite() != null && page.getSite().trim().length() > 0 ? page.getSite() : sC.getSiteName(), page.getName(), useClass );
            }
            catch( Exception e )
            {
                log.error( "Error creating instance of [" + page.getClazz() + "]" );
            }
        }
    }

    /**
     * Parses the test.
     *
     * @param xTest the x test
     * @param typeName the type name
     * @return the key word test
     */
    private KeyWordTest parseTest( XTest xTest )
    { 
        KeyWordTest test = new KeyWordTest( xTest.getName(), xTest.isActive(), xTest.getDataProvider(), xTest.getDataDriver(), xTest.isTimed(), xTest.getLinkId(), xTest.getOs(), xTest.getThreshold(), xTest.getDescription() != null ? xTest.getDescription().getValue() : null, xTest.getTagNames(), xTest.getContentKeys(), xTest.getDeviceTags(), configProperties, xTest.getCount(), null, null, null, null, xTest.getPriority(), xTest.getSeverity(), xTest.getTrace() );
        test.setReliesOn( xTest.getReliesOn() );
        
        KeyWordStep[] steps = parseSteps( xTest.getStep(), xTest.getName() );

        for (KeyWordStep step : steps)
            test.addStep( step );

        return test;
    }
    
    private KeyWordTest parseFunction( XFunction xTest )
    { 
        KeyWordTest test = new KeyWordTest( xTest.getName(), xTest.isActive(), xTest.getDataProvider(), null, false, xTest.getLinkId(), null, 0, xTest.getDescription() != null ? xTest.getDescription().getValue() : null, null, null, null, configProperties, 1, xTest.getInputPage(), xTest.getOutputPage(), xTest.getMode(), xTest.getOperations(), 0, 0, TRACE.OFF.name() );
        test.getExpectedParameters().addAll( parseParameters( xTest.getParameter() ) );
        
        KeyWordStep[] steps = parseSteps( xTest.getStep(), xTest.getName() );

        for (KeyWordStep step : steps)
            test.addStep( step );

        return test;
    }

    /**
     * Parses the steps.
     *
     * @param xSteps the x steps
     * @param testName the test name
     * @param typeName the type name
     * @return the key word step[]
     */
    private KeyWordStep[] parseSteps( List<XStep> xSteps, String testName )
    {

        if (log.isDebugEnabled())
            log.debug( "Extracted " + xSteps.size() + " Steps" );

        List<KeyWordStep> stepList = new ArrayList<KeyWordStep>( 10 );

        for ( XStep xStep : xSteps )
        {
            KeyWordStep step = KeyWordStepFactory.instance().createStep( xStep.getName(), xStep.getPage(), xStep.isActive(), xStep.getType(),
                                                                         xStep.getLinkId(), xStep.isTimed(), StepFailure.valueOf( xStep.getFailureMode() ), xStep.isInverse(),
                                                                         xStep.getOs(), xStep.getBrowser(), xStep.getPoi(), xStep.getThreshold().intValue(), "", xStep.getWait().intValue(),
                                                                         xStep.getContext(), xStep.getValidation(), xStep.getDevice(),
                                                                         (xStep.getValidationType() != null && !xStep.getValidationType().isEmpty() ) ? ValidationType.valueOf( xStep.getValidationType() ) : null, xStep.getTagNames(), 
                                                                         xStep.isStartAt(), xStep.isBreakpoint(), xStep.getDeviceTags(), xStep.getSite(), configProperties, xStep.getVersion(), 
                                                                         xStep.getAppContext(), xStep.getWaitFor(), xStep.isTrace(), null, null, xStep.isAllowMultiple() );
            
            step.getParameterList().addAll( parseParameters( xStep.getParameter() ) );
            parseTokens( xStep.getToken(), testName, xStep.getName(), step );
            
            step.addAllSteps( parseSteps( xStep.getStep(), testName ) );
            stepList.add( step );
        }

        return stepList.toArray( new KeyWordStep[0] );
    }

    /**
     * Parses the parameters.
     *
     * @param pList the list
     * @param testName the test name
     * @param stepName the step name
     * @param typeName the type name
     * @param parentStep the parent step
     * @return the key word parameter[]
     */
    private List<KeyWordParameter> parseParameters( List<XParameter> pList )
    {
        if (log.isDebugEnabled())
            log.debug( "Extracted " + pList.size() + " Parameters" );
        
        List<KeyWordParameter> kList = new ArrayList<KeyWordParameter>( 10 );
        
        for ( XParameter p : pList )
        {
            
            ParameterType ptype = ParameterType.STATIC;
            if ( p.getType() != null )
                ptype = ParameterType.valueOf( p.getType() );
            
            KeyWordParameter kp = new KeyWordParameter( ptype, p.getValue(), p.getName(), p.getUsage() );
            
            if ( p.getToken() != null && !p.getToken().isEmpty() )
            {
                for ( XToken t : p.getToken() )
                {
                    kp.addToken( new KeyWordToken( TokenType.valueOf(t.getType() ), t.getValue(), t.getName() ) );
                }
            }
            
            if ( p.equals( ParameterType.FILE ) )
            {
                File dataFile = new File( p.getValue() );
                if ( dataFile.isFile() )
                {
                    try
                    {
                        kp.setValue( readIFile( new FileInputStream( dataFile ) ) );
                        kp.setFileName( dataFile.getAbsolutePath() );
                    }
                    catch( FileNotFoundException e )
                    {
                        log.error( "Error reading parameter file", e );
                    }
                }
                else
                {
                    dataFile = new File( configFolder, p.getValue() );
                    if ( dataFile.isFile() )
                    {
                        try
                        {
                            kp.setValue( readIFile( new FileInputStream( dataFile ) ) );
                            kp.setFileName( dataFile.getAbsolutePath() );
                        }
                        catch( FileNotFoundException e )
                        {
                            log.error( "Error reading parameter file", e );
                        }
                    }
                }  
            }
            
            kList.add( kp );
        }
        
        return kList;
    }
    
    private String readIFile( InputStream inputStream )
    {
        
        try
        {
            StringBuilder stringBuilder = new StringBuilder();
            int bytesRead = 0;
            byte[] buffer = new byte[512];

            while ( ( bytesRead = inputStream.read( buffer ) ) > 0 )
            {
                stringBuilder.append( new String( buffer, 0, bytesRead ) );
            }
            
            return stringBuilder.toString();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            try { inputStream.close(); } catch( Exception e ) {}
        }
        
        return null;
    }

    /**
     * Parses the tokens.
     *
     * @param tList the t list
     * @param testName the test name
     * @param stepName the step name
     * @param typeName the type name
     * @param parentStep the parent step
     * @return the key word token[]
     */
    private void parseTokens( List<XToken> tList, String testName, String stepName, KeyWordStep parentStep )
    {
        if (log.isDebugEnabled())
            log.debug( "Extracted " + tList + " Tokens" );

        for ( XToken t : tList )
        {
            parentStep.addToken( new KeyWordToken( TokenType.valueOf(t.getType() ), t.getValue(), t.getName() ) );
        }

    }

    

    @Override
    protected boolean _executeTest( SuiteContainer sC) throws Exception
    {
        switch ( xRoot.getSuite().getProvider() )
        {
        		case "GHERKIN":
            case "XML":
            case "SQL":
            case "EXCEL": 
            case "LOCAL":
            case "LOCAL-SQL":
            {
                runTest( xRoot.getDriver().getOutputFolder(), XMLTestDriver.class, sC );
                break;
            }

        }
        return true;
    }
    
    private ThreadLocal<Element> previousElement = new ThreadLocal<Element>();
    private ThreadLocal<ElementDescriptor> previousElementDescriptor = new ThreadLocal<ElementDescriptor>();
    
    @Override
    public void setCachedElement( Element cachedElement, ElementDescriptor elementDescriptor )
    {
        this.previousElement.set( cachedElement );
        this.previousElementDescriptor.set( elementDescriptor );
        
    }
    
    public Element getElement( ElementDescriptor elementDescriptor )
    {
        
        if ( previousElement.get() != null && previousElementDescriptor.get() != null && elementDescriptor.equals( previousElementDescriptor.get() ) )
        {
            return previousElement.get();
        }
        else
        {
            previousElement.set( null );
            previousElementDescriptor.set( null );
        }
        
        Element returnElement = null;
        if ( internalElementProvider != null )
            returnElement = internalElementProvider.getElement( elementDescriptor );
        
        if ( returnElement == null )
            returnElement = elementMap.get(  elementDescriptor.toString() );
        
        if ( returnElement != null )
        {
            previousElement.set( returnElement );
            previousElementDescriptor.set( elementDescriptor );
        }
        
        return returnElement;
    }
    
    @Override
    public List<SiteContainer> getSiteList()
    {
        return siteList;
    }
    
    /**
     * Configure the proxy settings from the driver config file
     * @return
     */
    protected boolean configureProxy()
    {
    	if ( xRoot.getProxy() != null && xRoot.getProxy().getProxyHost() != null 
    			&& !xRoot.getProxy().getProxyHost().isEmpty() 
    			&& Integer.parseInt( xRoot.getProxy().getProxyPort() ) > 0 )
        {
            log.info( "Proxy configured as " + xRoot.getProxy().getProxyHost() + ":" + xRoot.getProxy().getProxyPort() );
            ProxyRegistry.instance().setProxyHost(xRoot.getProxy().getProxyHost());
            ProxyRegistry.instance().setProxyPort(xRoot.getProxy().getProxyPort());
            ProxyRegistry.instance().setIgnoreHost(xRoot.getProxy().getProxyIgnoreHost());
            ProxyRegistry.instance().setProxyUser( xRoot.getProxy().getProxyUser() );
            ProxyRegistry.instance().setProxyPassword( xRoot.getProxy().getProxyPassword() );
            if ( xRoot.getProxy().getProxyAuthenticator() != null && !xRoot.getProxy().getProxyAuthenticator().isEmpty() )
            {
                try
                {
                    ProxyRegistry.instance().setProxyAuthenticator( Class.forName( xRoot.getProxy().getProxyAuthenticator() ) );
                }
                catch( Exception e )
                {
                    log.error( "Could not create proxy authenticator class defined as " + xRoot.getProxy().getProxyAuthenticator(), e );
                }
            }
            
            ProxyRegistry.instance().registerAuthenticator();
        }
        return true;
    }

    private ElementProvider internalElementProvider;
    @Override
    public void addElementProvider( ElementProvider elementProvider )
    {
        this.internalElementProvider = elementProvider;
        
    }

}
