package app.architecture;

import org.junit.jupiter.api.DisplayName;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "app", importOptions = ImportOption.DoNotIncludeTests.class)
public 
@DisplayName("Pruebas unitarias de HexagonalArchitecture")
class HexagonalArchitectureTest {

        @ArchTest
        static final ArchRule domain_should_not_depend_on_infrastructure = noClasses()
                        .that().resideInAPackage("..domain..")
                        .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

        @ArchTest
        static final ArchRule domain_should_not_depend_on_application = noClasses()
                        .that().resideInAPackage("..domain..")
                        .should().dependOnClassesThat().resideInAPackage("..application..");

        @ArchTest
        static final ArchRule application_should_not_depend_on_infrastructure_implementations = noClasses()
                        .that().resideInAPackage("..application..")
                        .should().dependOnClassesThat().resideInAPackage("..adapter.out.persistence.adapter..");

        @ArchTest
        static final ArchRule infrastructure_adapters_should_depend_on_domain_ports = classes()
                        .that().resideInAPackage("..adapter.out.persistence.adapter..")
                        .should().dependOnClassesThat().resideInAPackage("..domain.ports..");

        @ArchTest
        static final ArchRule service_names_should_end_with_service_or_usecase = classes()
                        .that().resideInAPackage("..application.usecase..")
                        .and().areNotNestedClasses()
                        .should().haveSimpleNameEndingWith("UseCase");

        @ArchTest
        static final ArchRule repository_names_should_end_with_repository = classes()
                        .that().resideInAPackage("..adapter.out.persistence.repository..")
                        .should().haveSimpleNameEndingWith("Repository");
}
