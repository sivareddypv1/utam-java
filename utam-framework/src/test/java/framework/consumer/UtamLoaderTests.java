package framework.consumer;

import framework.base.PageMarker;
import framework.base.RootPageObject;
import framework.consumer.impl.PageObjectMockImpl;
import framework.context.DefaultProfileContext;
import framework.context.Profile;
import framework.context.ProfileContext;
import framework.context.StringValueProfile;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;
import selenium.context.SeleniumContext;
import selenium.element.LocatorNode;
import selenium.element.Web;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import static framework.consumer.UtamLoaderConfigTests.getConfig;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static selenium.context.SeleniumContextProvider.DEFAULT_POLLING_INTERVAL;
import static selenium.context.SeleniumContextProvider.DEFAULT_POLLING_TIMEOUT;
import static selenium.element.LocatorUtilities.getAllNodes;
import static selenium.element.LocatorUtilities.getElementLocator;

/**
 * @author elizaveta.ivanova
 * @since 230
 */
public class UtamLoaderTests {

  static UtamLoaderImpl getLoader() {
    UtamLoaderConfigImpl config = getConfig();
    return new UtamLoaderImpl(config, mock(WebDriver.class));
  }

  @Test
  public void testRootPageObjectCreation() {
    UtamLoaderImpl loader = getLoader();
    RootPageObject rootPageObject = loader.create(PageObjectMock.class);
    assertThat(rootPageObject, is(instanceOf(PageObjectMock.class)));
  }

  @Test
  public void testPageObjectLoad() {
    UtamLoaderImpl loader = getLoader();
    RootPageObject rootPageObject = loader.load(PageObjectMock.class);
    assertThat(rootPageObject, is(instanceOf(PageObjectMock.class)));
  }

  @Test
  public void testCreateUtamFromContainer() {
    UtamLoaderImpl loader = getLoader();
    ContainerMock containerMock = new ContainerMock();
    PageObjectMock pageObjectMock =
        loader.create(containerMock, PageObjectMock.class, Web.byCss("root"));
    List<LocatorNode> chain = getAllNodes(getElementLocator(pageObjectMock.getRoot()));
    assertThat(chain.get(1).getSelector().getValue(), is(equalTo("root")));
  }

  @Test
  public void testProfileBasedDependency() {
    UtamLoaderConfig config = getConfig();
    Profile profile = new StringValueProfile("profile", "name");
    ProfileContext profileContext = new DefaultProfileContext(profile);
    profileContext.setBean(PageObjectMock.class, PageObjectMockForProfile.class.getName());
    config.setActiveProfile(profile);
    config.setProfileContext(profileContext);
    UtamLoader loader = new UtamLoaderImpl(config, mock(WebDriver.class));
    RootPageObject rootPageObject = loader.load(PageObjectMock.class);
    assertThat(rootPageObject, is(instanceOf(PageObjectMockForProfile.class)));
  }

  @Test
  public void testDefaultConstructor() {
    UtamLoaderImpl loader = new UtamLoaderImpl(mock(WebDriver.class));
    SeleniumContext seleniumContext = loader.utamConfig.getSeleniumContext();
    assertThat(seleniumContext.getPollingTimeout(), is(equalTo(DEFAULT_POLLING_TIMEOUT)));
    assertThat(seleniumContext.getPollingInterval(), is(equalTo(DEFAULT_POLLING_INTERVAL)));
  }

  @Test
  public void testDefaultConstructorForTests() {
    UtamLoaderImpl loader = (UtamLoaderImpl) UtamLoaderImpl.getSimulatorLoader(mock(WebDriver.class));
    SeleniumContext seleniumContext = loader.utamConfig.getSeleniumContext();
    assertThat(seleniumContext.getPollingTimeout(), is(equalTo(Duration.ofSeconds(1))));
    assertThat(seleniumContext.getPollingInterval(), is(equalTo(DEFAULT_POLLING_INTERVAL)));
  }

  private static class ContainerMock implements Container {

    final Supplier<SearchContext> root = () -> mock(WebElement.class);

    @Override
    public Supplier<SearchContext> getScope() {
      return root;
    }
  }

  @PageMarker.Find(css = "root")
  // has to be public for reflections to work
  public static class PageObjectMockForProfile extends PageObjectMockImpl {}
}
