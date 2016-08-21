<#import "../template/page.ftl" as page/>

<@page.common title="Login">

<@page.heading/>

<#if loginError??>
<div class="alert alert-danger" role="alert">
  <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
  <span class="sr-only">Error:</span>Invalid login or password. Generated at ${currentTime?c}.
</div>
</#if>

<div class="container">
<div class="row">
  <div class="col-md-3"></div>
  <div class="col-md-6 well">

  <form method="POST" action="/g/execlogin" class="signin-form">
    <h3>Please Sign In</h3>

    <div>
      <label for="username-input" class="sr-only">Login</label>
      <input id="username-input" type="text" class="form-control" name="username" placeholder="Login" required autofocus />
    </div>
    <div>
      <label for="password-input" class="sr-only">Password</label>
      <input id="password-input" type="password" class="form-control" name="password" placeholder="Password" required />
    </div>
    <div>
      <label for="remember-me-input">
        <input id="remember-me-input" type="checkbox" name="remember-me" /><span>&nbsp;Remember Me</span>
      </label>
    </div>
    <div class="button-holder">
      <input type="submit" class="btn btn-lg btn-primary btn-block" value="Login" />
    </div>
    <div class="button-holder">
      <input type="reset" class="btn btn-lg btn-warning btn-block" value="Reset" />
    </div>
  </form>
  </div> <!-- /.well -->
  <div class="col-md-3"></div>
</div> <!-- /.row -->
</div> <!-- /.container -->

</@page.common>