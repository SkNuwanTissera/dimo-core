package unit;

import app.DimoApplication;
import app.entities.User;
import app.exceptions.service.EmailAlreadyInUseException;
import app.exceptions.service.UsernameAlreadyExistsException;
import app.exceptions.service.UsernameDoesNotExistException;
import app.repositories.UserRepository;
import app.security.Authorities;
import app.security.SecurityConfiguration;
import app.services.UserService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;


@RunWith ( SpringRunner.class )
@SpringBootTest ( classes = DimoApplication.class )
@ActiveProfiles ("unit-tests")
public class UserServiceTests
{

    @Autowired
    @InjectMocks
    private UserService userService;

    @Mock
    UserRepository userRepository;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup ()
    {
        MockitoAnnotations.initMocks( this );
    }

    @Test
    public void create ()
    {
        User user = new User();
        user.setUsername( "testUsername" );
        user.setEmail( "test@mail.com" );
        user.setPassword( "testPassword" );
        when( this.userRepository.save( user ) ).thenReturn( user );
        when( this.userRepository.findByUsername( user.getUsername() ) ).thenReturn( Optional.empty() );
        when( this.userRepository.findByEmail( user.getEmail() ) ).thenReturn( Optional.empty() );

        this.userService.createUser( user );

        assertThat( user.getUsername(), is( "testUsername" ) );
        assertThat( user.getEmail(), is( "test@mail.com" ) );
        assertThat( SecurityConfiguration.passwordEncoder.matches( "testPassword", user.getPassword() ), is( true ) );
        assertThat( user.getAuthorities().size(), is( 1 ) );
        assertThat( user.getAuthorities().stream().map( GrantedAuthority::getAuthority )
                .anyMatch( auth -> auth.equals( Authorities.User ) ), is( true ) );
    }

    @Test
    public void createUsernameAlreadyExists ()
    {
        User user = new User();
        user.setUsername( "testUsername" );
        user.setEmail( "test@mail.com" );
        user.setPassword( "testPassword" );
        when( this.userRepository.findByUsername( user.getUsername() ) ).thenReturn( Optional.of( user ) );
        when( this.userRepository.findByEmail( user.getEmail() ) ).thenReturn( Optional.empty() );

        this.thrown.expect( UsernameAlreadyExistsException.class );
        this.userService.createUser( user );
    }

    @Test
    public void createEmailAlreadyExists ()
    {
        User user = new User();
        user.setUsername( "testUsername" );
        user.setEmail( "test@mail.com" );
        user.setPassword( "testPassword" );
        when( this.userRepository.findByUsername( user.getUsername() ) ).thenReturn( Optional.empty() );
        when( this.userRepository.findByEmail( user.getEmail() ) ).thenReturn( Optional.of( user ) );

        this.thrown.expect( EmailAlreadyInUseException.class );
        this.userService.createUser( user );
    }

    @Test
    public void delete ()
    {
        when( this.userRepository.findByUsername( "testUsername" ) ).thenReturn( Optional.of( new User() ) );
        this.userService.deleteUser( "testUsername" );
    }

    @Test
    public void deleteUsernameDoesNotExist ()
    {
        when( this.userRepository.findByUsername( "testUsername" ) ).thenReturn( Optional.empty() );
        this.thrown.expect( UsernameDoesNotExistException.class );
        this.userService.deleteUser( "testUsername" );
    }

}
